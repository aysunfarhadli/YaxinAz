package com.yaxinaz.issue;

import com.yaxinaz.issue.dto.DuplicateCandidateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Spec section 39: before an issue is created, surface likely-duplicate reports already open in the
 * same community so the resident can follow the existing one instead of fragmenting reports. Pure
 * Java decision (see {@link DuplicateIssueMatcher}) - AI is not involved in the final call, only
 * Java's own keyword/location heuristic. Never auto-merges; only ever returns candidates for the
 * resident to explicitly confirm against.
 */
@Service
@RequiredArgsConstructor
public class DuplicateDetectionService {

    private static final List<IssueStatus> UNRESOLVED_STATUSES = List.of(
            IssueStatus.OPEN, IssueStatus.ACKNOWLEDGED, IssueStatus.IN_PROGRESS, IssueStatus.WAITING_FOR_VENDOR);

    private static final int MAX_CANDIDATES = 3;

    private final IssueRepository issueRepository;
    private final IssueSupportRepository issueSupportRepository;

    @Transactional(readOnly = true)
    public List<DuplicateCandidateResponse> findCandidates(
            Long communityId, IssueCategory category, String buildingOrLocation, String text) {

        Specification<Issue> spec = Specification.allOf(
                IssueSpecifications.notDeleted(),
                IssueSpecifications.inCommunity(communityId),
                IssueSpecifications.hasCategory(category),
                IssueSpecifications.statusIn(UNRESOLVED_STATUSES)
        );
        List<Issue> candidates = issueRepository.findAll(spec);
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<Long> candidateIds = candidates.stream().map(Issue::getId).toList();
        Map<Long, Long> supportCounts = issueSupportRepository.countByIssueIdIn(candidateIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        IssueSupportRepository.IssueSupportCount::getIssueId,
                        IssueSupportRepository.IssueSupportCount::getTotal));

        return candidates.stream()
                .map(issue -> {
                    double score = DuplicateIssueMatcher.score(
                            buildingOrLocation, text,
                            issue.getBuildingOrLocation(), issue.getTitle() + " " + issue.getDescription());
                    return new DuplicateCandidateResponse(
                            issue.getId(), issue.getTitle(), issue.getCategory(), issue.getStatus(),
                            issue.getBuildingOrLocation(), supportCounts.getOrDefault(issue.getId(), 0L), score);
                })
                .filter(candidate -> candidate.similarityScore() >= DuplicateIssueMatcher.SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(DuplicateCandidateResponse::similarityScore).reversed())
                .limit(MAX_CANDIDATES)
                .toList();
    }
}

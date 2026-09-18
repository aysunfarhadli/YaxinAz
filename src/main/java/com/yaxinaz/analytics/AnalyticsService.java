package com.yaxinaz.analytics;

import com.yaxinaz.analytics.dto.StatisticsResponse;
import com.yaxinaz.community.CommunityMembershipRepository;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.community.MembershipStatus;
import com.yaxinaz.event.CommunityEventRepository;
import com.yaxinaz.feed.PostRepository;
import com.yaxinaz.issue.Issue;
import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueRepository;
import com.yaxinaz.issue.IssueStatus;
import com.yaxinaz.issue.IssueSupportRepository;
import com.yaxinaz.issue.dto.IssueSummaryResponse;
import com.yaxinaz.provider.ProviderProfile;
import com.yaxinaz.provider.ProviderProfileRepository;
import com.yaxinaz.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All numbers here are computed in Java from real repository data using Streams (spec section 57) -
 * nothing here is guessed or AI-generated. This is deliberately the ONLY source of truth the AI
 * community-summary/insights features (Phase 12b) are allowed to narrate.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final List<IssueStatus> UNRESOLVED_STATUSES = List.of(
            IssueStatus.OPEN, IssueStatus.ACKNOWLEDGED, IssueStatus.IN_PROGRESS, IssueStatus.WAITING_FOR_VENDOR);
    private static final List<IssueStatus> RESOLVED_STATUSES = List.of(IssueStatus.RESOLVED, IssueStatus.CLOSED);
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    private final IssueRepository issueRepository;
    private final IssueSupportRepository issueSupportRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final CommunityEventRepository eventRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final CommunityService communityService;

    @Transactional(readOnly = true)
    @Cacheable(value = "communityStatistics", key = "#communityId")
    public StatisticsResponse computeStatistics(Long communityId) {
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), communityId);

        List<Issue> issues = issueRepository.findAllByCommunityIdAndDeletedFalse(communityId);

        long total = issues.size();
        long open = issues.stream().filter(i -> UNRESOLVED_STATUSES.contains(i.getStatus())).count();
        long resolved = issues.stream().filter(i -> RESOLVED_STATUSES.contains(i.getStatus())).count();
        Instant sevenDaysAgoForResolution = Instant.now().minus(Duration.ofDays(7));
        long resolvedThisWeek = issues.stream()
                .filter(i -> i.getResolvedAt() != null && i.getResolvedAt().isAfter(sevenDaysAgoForResolution))
                .count();
        long critical = issues.stream().filter(i -> i.getPriority() == IssuePriority.CRITICAL).count();
        long high = issues.stream().filter(i -> i.getPriority() == IssuePriority.HIGH).count();
        long stale = issues.stream().filter(Issue::isStale).count();
        long escalated = issues.stream().filter(Issue::isEscalated).count();

        double resolutionRate = total == 0 ? 0.0 : (resolved * 100.0) / total;

        double avgResolutionHours = issues.stream()
                .filter(i -> i.getResolvedAt() != null)
                .mapToDouble(i -> Duration.between(i.getCreatedAt(), i.getResolvedAt()).toMinutes() / 60.0)
                .average()
                .orElse(0.0);

        Map<IssueCategory, Long> byCategory = issues.stream()
                .collect(Collectors.groupingBy(Issue::getCategory, Collectors.counting()));
        Map<IssuePriority, Long> byPriority = issues.stream()
                .collect(Collectors.groupingBy(Issue::getPriority, Collectors.counting()));
        Map<IssueStatus, Long> byStatus = issues.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));

        IssueCategory mostCommonCategory = byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<String, Long> dailyActivity = last7DaysActivity(issues);

        IssueSummaryResponse highestImpact = highestImpactUnresolvedIssue(issues);

        long approvedMembers = membershipRepository.countByCommunityIdAndStatus(communityId, MembershipStatus.APPROVED);
        long totalPosts = postRepository.countByCommunityIdAndDeletedFalse(communityId);
        long totalEvents = eventRepository.countByCommunityIdAndDeletedFalse(communityId);

        List<ProviderProfile> providers = providerProfileRepository.findAll();
        double platformAvgRating = providers.stream()
                .filter(p -> p.getReviewCount() > 0)
                .mapToDouble(ProviderProfile::getAverageRating)
                .average()
                .orElse(0.0);
        long platformTotalReviews = providers.stream().mapToLong(ProviderProfile::getReviewCount).sum();

        return new StatisticsResponse(
                total, open, resolved, resolvedThisWeek, critical, high, stale, escalated,
                round2(resolutionRate), round2(avgResolutionHours),
                byCategory, byPriority, byStatus, dailyActivity, mostCommonCategory, highestImpact,
                approvedMembers, totalPosts, totalEvents, round2(platformAvgRating), platformTotalReviews);
    }

    private Map<String, Long> last7DaysActivity(List<Issue> issues) {
        Instant sevenDaysAgo = Instant.now().minus(Duration.ofDays(7));
        Map<String, Long> counts = issues.stream()
                .filter(i -> i.getCreatedAt().isAfter(sevenDaysAgo))
                .collect(Collectors.groupingBy(i -> DAY_FORMATTER.format(i.getCreatedAt()), Collectors.counting()));

        Map<String, Long> ordered = new LinkedHashMap<>();
        Stream.iterate(6, d -> d - 1).limit(7)
                .map(daysAgo -> DAY_FORMATTER.format(Instant.now().minus(Duration.ofDays(daysAgo))))
                .forEach(day -> ordered.put(day, counts.getOrDefault(day, 0L)));
        return ordered;
    }

    private IssueSummaryResponse highestImpactUnresolvedIssue(List<Issue> issues) {
        List<Issue> unresolved = issues.stream()
                .filter(i -> UNRESOLVED_STATUSES.contains(i.getStatus()))
                .toList();
        if (unresolved.isEmpty()) {
            return null;
        }
        List<Long> ids = unresolved.stream().map(Issue::getId).toList();
        Map<Long, Long> supportCounts = issueSupportRepository.countByIssueIdIn(ids).stream()
                .collect(Collectors.toMap(
                        IssueSupportRepository.IssueSupportCount::getIssueId,
                        IssueSupportRepository.IssueSupportCount::getTotal));

        Issue top = unresolved.stream()
                .max(Comparator.comparingLong(i -> supportCounts.getOrDefault(i.getId(), 0L)))
                .orElseThrow();

        long supportCount = supportCounts.getOrDefault(top.getId(), 0L);
        return new IssueSummaryResponse(top.getId(), top.getTitle(), top.getCategory(), top.getPriority(),
                top.getStatus(), top.getBuildingOrLocation(), supportCount, top.isStale(), top.isEscalated(),
                top.getCreatedAt());
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

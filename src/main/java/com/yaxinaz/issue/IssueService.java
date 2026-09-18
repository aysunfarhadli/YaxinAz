package com.yaxinaz.issue;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.config.properties.RateLimitProperties;
import com.yaxinaz.exception.IssueAlreadySupportedException;
import com.yaxinaz.exception.IssueNotFoundException;
import com.yaxinaz.exception.InvalidIssueStatusTransitionException;
import com.yaxinaz.exception.RateLimitExceededException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.issue.event.IssueStatusChangedEvent;
import com.yaxinaz.issue.dto.CommentResponse;
import com.yaxinaz.issue.dto.CreateCommentRequest;
import com.yaxinaz.issue.dto.CreateIssueRequest;
import com.yaxinaz.issue.dto.IssueActivityResponse;
import com.yaxinaz.issue.dto.IssueResponse;
import com.yaxinaz.issue.dto.IssueSummaryResponse;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.security.ratelimit.RateLimiterService;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private static final Logger log = LoggerFactory.getLogger(IssueService.class);

    private final IssueRepository issueRepository;
    private final IssueSupportRepository issueSupportRepository;
    private final IssueActivityRepository issueActivityRepository;
    private final IssueCommentRepository issueCommentRepository;
    private final UserRepository userRepository;
    private final CommunityService communityService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;

    @Transactional
    public IssueResponse createIssue(CreateIssueRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Community community = communityService.getCommunityOrThrow(request.communityId());
        communityService.requireApprovedMember(userId, community.getId());

        if (!rateLimiterService.tryConsume("issue-create:" + userId, rateLimitProperties.issueCreationPerMinute())) {
            throw new RateLimitExceededException("Too many issues reported. Please wait a minute and try again.");
        }

        Issue issue = Issue.builder()
                .community(community)
                .createdBy(userId)
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category())
                .priority(request.priority() != null ? request.priority() : IssuePriority.MEDIUM)
                .status(IssueStatus.OPEN)
                .buildingOrLocation(request.buildingOrLocation())
                .imageUrl(request.imageUrl())
                .aiSummary(request.aiSummary())
                .build();
        issue = issueRepository.save(issue);

        recordActivity(issue, IssueActivityType.CREATED, "Issue reported by resident", userId);
        if (request.aiSummary() != null && !request.aiSummary().isBlank()) {
            recordActivity(issue, IssueActivityType.AI_ANALYZED, "AI analyzed the report", null);
        }
        log.info("ISSUE_CREATED issueId={} communityId={} userId={}", issue.getId(), community.getId(), userId);
        auditLogService.record(userId, AuditActionType.ISSUE_CREATED, "Issue", issue.getId(), null, issue.getTitle());

        return toResponse(issue);
    }

    @Transactional(readOnly = true)
    public PagedResponse<IssueSummaryResponse> listIssues(
            Long communityId, IssueStatus status, IssuePriority priority, IssueCategory category,
            Boolean stale, Boolean escalated, Instant from, Instant to, Pageable pageable) {

        Community community = communityService.getCommunityOrThrow(communityId);
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), community.getId());

        Specification<Issue> spec = Specification.allOf(
                IssueSpecifications.notDeleted(),
                IssueSpecifications.inCommunity(communityId),
                IssueSpecifications.hasStatus(status),
                IssueSpecifications.hasPriority(priority),
                IssueSpecifications.hasCategory(category),
                IssueSpecifications.isStale(stale),
                IssueSpecifications.isEscalated(escalated),
                IssueSpecifications.createdAfter(from),
                IssueSpecifications.createdBefore(to)
        );

        Page<Issue> page = issueRepository.findAll(spec, pageable);
        List<Long> issueIds = page.getContent().stream().map(Issue::getId).toList();
        Map<Long, Long> supportCounts = issueIds.isEmpty()
                ? Map.of()
                : issueSupportRepository.countByIssueIdIn(issueIds).stream()
                        .collect(Collectors.toMap(IssueSupportRepository.IssueSupportCount::getIssueId,
                                IssueSupportRepository.IssueSupportCount::getTotal));

        return PagedResponse.of(page, issue -> toSummaryResponse(issue, supportCounts.getOrDefault(issue.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssue(Long issueId) {
        Issue issue = getIssueOrThrow(issueId);
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), issue.getCommunity().getId());
        return toResponse(issue);
    }

    @Transactional
    public IssueResponse updateStatus(Long issueId, IssueStatus newStatus) {
        Issue issue = getIssueOrThrow(issueId);
        communityService.requireCommunityAdmin(issue.getCommunity());

        IssueStatus current = issue.getStatus();
        if (!IssueStatusTransitionPolicy.isAllowed(current, newStatus)) {
            throw new InvalidIssueStatusTransitionException(
                    "Cannot transition issue from " + current + " to " + newStatus);
        }

        boolean reopening = IssueStatusTransitionPolicy.isReopen(current, newStatus);
        issue.setStatus(newStatus);
        if (newStatus == IssueStatus.RESOLVED) {
            issue.setResolvedAt(Instant.now());
        }
        if (reopening) {
            issue.setResolvedAt(null);
        }

        Long actorId = SecurityUtils.currentUserId();
        recordActivity(issue, reopening ? IssueActivityType.REOPENED : IssueActivityType.STATUS_CHANGED,
                "Status changed from " + current + " to " + newStatus, actorId);
        if (newStatus == IssueStatus.RESOLVED) {
            recordActivity(issue, IssueActivityType.RESOLVED, "Issue marked resolved", actorId);
        }
        log.info("ISSUE_STATUS_CHANGED issueId={} from={} to={} actorId={}", issueId, current, newStatus, actorId);
        auditLogService.record(actorId, AuditActionType.ISSUE_STATUS_CHANGED, "Issue", issueId,
                current.name(), newStatus.name());
        eventPublisher.publishEvent(new IssueStatusChangedEvent(
                issue.getId(), issue.getCommunity().getId(), issue.getCreatedBy(), current, newStatus, Instant.now()));

        return toResponse(issue);
    }

    @Transactional
    public IssueResponse updatePriority(Long issueId, IssuePriority newPriority) {
        Issue issue = getIssueOrThrow(issueId);
        communityService.requireCommunityAdmin(issue.getCommunity());

        IssuePriority current = issue.getPriority();
        issue.setPriority(newPriority);
        Long actorId = SecurityUtils.currentUserId();
        recordActivity(issue, IssueActivityType.PRIORITY_CHANGED,
                "Priority changed from " + current + " to " + newPriority, actorId);
        log.info("ISSUE_PRIORITY_CHANGED issueId={} from={} to={} actorId={}", issueId, current, newPriority, actorId);
        auditLogService.record(actorId, AuditActionType.ISSUE_PRIORITY_CHANGED, "Issue", issueId,
                current.name(), newPriority.name());

        return toResponse(issue);
    }

    @Transactional
    public void supportIssue(Long issueId) {
        Issue issue = getIssueOrThrow(issueId);
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, issue.getCommunity().getId());

        if (issueSupportRepository.existsByIssueIdAndUserId(issueId, userId)) {
            throw new IssueAlreadySupportedException("You have already marked this issue as affecting you");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));

        issueSupportRepository.save(IssueSupport.builder().issue(issue).user(user).build());
        log.info("ISSUE_SUPPORTED issueId={} userId={}", issueId, userId);
    }

    @Transactional(readOnly = true)
    public List<IssueActivityResponse> getTimeline(Long issueId) {
        Issue issue = getIssueOrThrow(issueId);
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), issue.getCommunity().getId());
        return issueActivityRepository.findAllByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(a -> new IssueActivityResponse(a.getId(), a.getType(), a.getMessage(), a.getActorUserId(), a.getCreatedAt()))
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long issueId, CreateCommentRequest request) {
        Issue issue = getIssueOrThrow(issueId);
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, issue.getCommunity().getId());

        User author = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));

        IssueComment comment = issueCommentRepository.save(
                IssueComment.builder().issue(issue).author(author).content(request.content().trim()).build());
        recordActivity(issue, IssueActivityType.COMMENT_ADDED, "New comment added", userId);

        return new CommentResponse(comment.getId(), issueId, author.getId(), author.getFullName(),
                comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> listComments(Long issueId, Pageable pageable) {
        Issue issue = getIssueOrThrow(issueId);
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), issue.getCommunity().getId());

        Page<IssueComment> page = issueCommentRepository.findAllByIssueIdOrderByCreatedAtAsc(issueId, pageable);
        return PagedResponse.of(page, c -> new CommentResponse(
                c.getId(), issueId, c.getAuthor().getId(), c.getAuthor().getFullName(),
                c.getContent(), c.getCreatedAt(), c.getUpdatedAt()));
    }

    private void recordActivity(Issue issue, IssueActivityType type, String message, Long actorUserId) {
        issueActivityRepository.save(IssueActivity.builder()
                .issue(issue).type(type).message(message).actorUserId(actorUserId).build());
    }

    private Issue getIssueOrThrow(Long issueId) {
        return issueRepository.findByIdAndDeletedFalse(issueId)
                .orElseThrow(() -> new IssueNotFoundException(issueId));
    }

    private IssueSummaryResponse toSummaryResponse(Issue issue, long supportCount) {
        return new IssueSummaryResponse(
                issue.getId(), issue.getTitle(), issue.getCategory(), issue.getPriority(), issue.getStatus(),
                issue.getBuildingOrLocation(), supportCount, issue.isStale(), issue.isEscalated(), issue.getCreatedAt());
    }

    private IssueResponse toResponse(Issue issue) {
        String reporterName = userRepository.findByIdAndDeletedFalse(issue.getCreatedBy())
                .map(User::getFullName)
                .orElse("Unknown resident");
        long supportCount = issueSupportRepository.countByIssueId(issue.getId());
        long commentCount = issueCommentRepository.countByIssueId(issue.getId());

        return new IssueResponse(
                issue.getId(), issue.getCommunity().getId(), issue.getCommunity().getName(),
                issue.getCreatedBy(), reporterName, issue.getTitle(), issue.getDescription(), issue.getAiSummary(),
                issue.getCategory(), issue.getPriority(), issue.getStatus(), issue.getBuildingOrLocation(),
                issue.getImageUrl(), issue.isStale(), issue.isEscalated(), supportCount, commentCount,
                issue.getCreatedAt(), issue.getUpdatedAt(), issue.getResolvedAt());
    }
}

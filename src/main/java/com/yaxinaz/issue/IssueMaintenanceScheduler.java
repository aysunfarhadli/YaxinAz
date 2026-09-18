package com.yaxinaz.issue;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.config.properties.EscalationProperties;
import com.yaxinaz.issue.event.IssueEscalatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Java-only stale/escalation rules (spec sections 43-45) - no AI involvement. Runs on a fixed
 * interval via Spring's scheduler (already enabled on the main application class).
 */
@Component
@RequiredArgsConstructor
public class IssueMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(IssueMaintenanceScheduler.class);

    private static final List<IssuePriority> STALE_PRIORITIES = List.of(IssuePriority.HIGH, IssuePriority.CRITICAL);
    private static final List<IssueStatus> STALE_STATUSES = List.of(IssueStatus.OPEN, IssueStatus.ACKNOWLEDGED);
    private static final List<IssueStatus> RESOLVED_LIKE_STATUSES =
            List.of(IssueStatus.RESOLVED, IssueStatus.CLOSED, IssueStatus.REJECTED);

    private final IssueRepository issueRepository;
    private final EscalationProperties escalationProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    @Scheduled(fixedRateString = "${yaxinaz.escalation.check-interval-ms:300000}")
    @Transactional
    public void checkStaleIssues() {
        Instant threshold = Instant.now().minusSeconds(escalationProperties.staleIssueHoursThreshold() * 3600L);
        List<Issue> candidates = issueRepository
                .findAllByPriorityInAndStatusInAndUpdatedAtBeforeAndStaleFalseAndDeletedFalse(
                        STALE_PRIORITIES, STALE_STATUSES, threshold);

        for (Issue issue : candidates) {
            issue.setStale(true);
            log.info("ISSUE_MARKED_STALE issueId={} priority={} status={} lastUpdated={}",
                    issue.getId(), issue.getPriority(), issue.getStatus(), issue.getUpdatedAt());
        }
    }

    @Scheduled(fixedRateString = "${yaxinaz.escalation.check-interval-ms:300000}")
    @Transactional
    public void checkEscalations() {
        Instant criticalThreshold = Instant.now().minusSeconds(escalationProperties.criticalOpenMinutesThreshold() * 60L);
        List<Issue> criticalCandidates = issueRepository
                .findAllByPriorityAndStatusAndCreatedAtBeforeAndEscalatedFalseAndDeletedFalse(
                        IssuePriority.CRITICAL, IssueStatus.OPEN, criticalThreshold);
        for (Issue issue : criticalCandidates) {
            escalate(issue, "CRITICAL issue left OPEN for more than "
                    + escalationProperties.criticalOpenMinutesThreshold() + " minutes");
        }

        Instant highThreshold = Instant.now().minusSeconds(escalationProperties.highUnresolvedHoursThreshold() * 3600L);
        List<Issue> highCandidates = issueRepository
                .findAllByPriorityAndCreatedAtBeforeAndEscalatedFalseAndDeletedFalseAndStatusNotIn(
                        IssuePriority.HIGH, highThreshold, RESOLVED_LIKE_STATUSES);
        for (Issue issue : highCandidates) {
            escalate(issue, "HIGH priority issue unresolved for more than "
                    + escalationProperties.highUnresolvedHoursThreshold() + " hours");
        }
    }

    private void escalate(Issue issue, String reason) {
        issue.setEscalated(true);
        log.warn("CRITICAL_ALERT_CREATED issueId={} communityId={} priority={} reason={}",
                issue.getId(), issue.getCommunity().getId(), issue.getPriority(), reason);
        auditLogService.record(null, AuditActionType.CRITICAL_ALERT_CREATED, "Issue", issue.getId(),
                null, reason);
        eventPublisher.publishEvent(new IssueEscalatedEvent(
                issue.getId(), issue.getCommunity().getId(), issue.getPriority(), reason, Instant.now()));
    }
}

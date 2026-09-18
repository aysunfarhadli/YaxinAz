package com.yaxinaz.issue.event;

import com.yaxinaz.issue.IssuePriority;

import java.time.Instant;

/**
 * Published by {@link com.yaxinaz.issue.IssueMaintenanceScheduler} when Java's escalation rules
 * (spec section 44) flip an issue to escalated=true. Deliberately a plain record (not a
 * notification/audit row itself) so later phases can each add their own listener - Phase 11
 * (notifications) and Phase 13 (audit log) both subscribe independently without this event or its
 * publisher needing to change.
 */
public record IssueEscalatedEvent(
        Long issueId,
        Long communityId,
        IssuePriority priority,
        String reason,
        Instant occurredAt
) {
}

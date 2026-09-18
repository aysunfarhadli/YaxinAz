package com.yaxinaz.issue.event;

import com.yaxinaz.issue.IssueStatus;

import java.time.Instant;

public record IssueStatusChangedEvent(
        Long issueId,
        Long communityId,
        Long reporterUserId,
        IssueStatus oldStatus,
        IssueStatus newStatus,
        Instant occurredAt
) {
}

package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssueActivityType;

import java.time.Instant;

public record IssueActivityResponse(
        Long id,
        IssueActivityType type,
        String message,
        Long actorUserId,
        Instant createdAt
) {
}

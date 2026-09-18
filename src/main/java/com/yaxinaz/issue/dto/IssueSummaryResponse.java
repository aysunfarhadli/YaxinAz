package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueStatus;

import java.time.Instant;

public record IssueSummaryResponse(
        Long id,
        String title,
        IssueCategory category,
        IssuePriority priority,
        IssueStatus status,
        String buildingOrLocation,
        long supportCount,
        boolean stale,
        boolean escalated,
        Instant createdAt
) {
}

package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueStatus;

import java.time.Instant;

public record IssueResponse(
        Long id,
        Long communityId,
        String communityName,
        Long createdBy,
        String reporterName,
        String title,
        String description,
        String aiSummary,
        IssueCategory category,
        IssuePriority priority,
        IssueStatus status,
        String buildingOrLocation,
        String imageUrl,
        boolean stale,
        boolean escalated,
        long supportCount,
        long commentCount,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt
) {
}

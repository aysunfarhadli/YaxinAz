package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssueStatus;

public record DuplicateCandidateResponse(
        Long issueId,
        String title,
        IssueCategory category,
        IssueStatus status,
        String buildingOrLocation,
        long supportCount,
        double similarityScore
) {
}

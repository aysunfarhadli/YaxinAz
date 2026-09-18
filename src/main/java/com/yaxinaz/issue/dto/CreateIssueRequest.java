package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIssueRequest(
        Long communityId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotNull IssueCategory category,
        IssuePriority priority,
        String buildingOrLocation,
        String imageUrl,
        @Size(max = 2000) String aiSummary
) {
}

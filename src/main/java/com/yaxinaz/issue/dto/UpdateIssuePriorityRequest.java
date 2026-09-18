package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssuePriority;
import jakarta.validation.constraints.NotNull;

public record UpdateIssuePriorityRequest(
        @NotNull IssuePriority priority
) {
}

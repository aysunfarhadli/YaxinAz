package com.yaxinaz.issue.dto;

import com.yaxinaz.issue.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateIssueStatusRequest(
        @NotNull IssueStatus status
) {
}

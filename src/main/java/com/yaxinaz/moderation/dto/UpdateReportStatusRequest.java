package com.yaxinaz.moderation.dto;

import com.yaxinaz.moderation.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReportStatusRequest(
        @NotNull ReportStatus status
) {
}

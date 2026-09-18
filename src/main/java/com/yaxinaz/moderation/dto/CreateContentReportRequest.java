package com.yaxinaz.moderation.dto;

import com.yaxinaz.moderation.ContentType;
import com.yaxinaz.moderation.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateContentReportRequest(
        @NotNull ContentType contentType,
        @NotNull Long contentId,
        @NotNull ReportReason reason,
        @Size(max = 1000) String description
) {
}

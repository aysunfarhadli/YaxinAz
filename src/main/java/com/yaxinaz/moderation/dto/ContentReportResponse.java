package com.yaxinaz.moderation.dto;

import com.yaxinaz.moderation.ContentType;
import com.yaxinaz.moderation.ReportReason;
import com.yaxinaz.moderation.ReportStatus;

import java.time.Instant;

public record ContentReportResponse(
        Long id,
        Long reportedBy,
        ContentType contentType,
        Long contentId,
        ReportReason reason,
        String description,
        ReportStatus status,
        Instant createdAt
) {
}

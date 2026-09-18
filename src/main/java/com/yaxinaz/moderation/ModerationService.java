package com.yaxinaz.moderation;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.config.properties.RateLimitProperties;
import com.yaxinaz.exception.ContentReportNotFoundException;
import com.yaxinaz.exception.RateLimitExceededException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.moderation.dto.ContentReportResponse;
import com.yaxinaz.moderation.dto.CreateContentReportRequest;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.security.ratelimit.RateLimiterService;
import com.yaxinaz.user.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    private final ContentReportRepository contentReportRepository;
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;
    private final AuditLogService auditLogService;

    @Transactional
    public ContentReportResponse createReport(CreateContentReportRequest request) {
        Long userId = SecurityUtils.currentUserId();
        if (!rateLimiterService.tryConsume("report:" + userId, rateLimitProperties.moderationReportPerMinute())) {
            throw new RateLimitExceededException("Too many reports submitted. Please wait a minute and try again.");
        }

        ContentReport report = contentReportRepository.save(ContentReport.builder()
                .reportedBy(userId)
                .contentType(request.contentType())
                .contentId(request.contentId())
                .reason(request.reason())
                .description(request.description())
                .status(ReportStatus.PENDING)
                .build());

        auditLogService.record(userId, AuditActionType.CONTENT_REPORTED,
                request.contentType().name(), request.contentId(), null, request.reason().name());
        log.info("CONTENT_REPORTED reportId={} contentType={} contentId={} reportedBy={}",
                report.getId(), request.contentType(), request.contentId(), userId);

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ContentReportResponse> listQueue(ReportStatus status, Pageable pageable) {
        requirePlatformAdmin();
        ReportStatus effective = status != null ? status : ReportStatus.PENDING;
        Page<ContentReport> page = contentReportRepository.findAllByStatus(effective, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    @Transactional
    public ContentReportResponse resolve(Long reportId, ReportStatus newStatus) {
        requirePlatformAdmin();
        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new ContentReportNotFoundException(reportId));

        ReportStatus previous = report.getStatus();
        report.setStatus(newStatus);

        Long adminId = SecurityUtils.currentUserId();
        if (newStatus == ReportStatus.ACTION_TAKEN) {
            auditLogService.record(adminId, AuditActionType.CONTENT_REMOVED,
                    report.getContentType().name(), report.getContentId(), previous.name(), newStatus.name());
        }
        log.info("MODERATION_REPORT_{} reportId={} adminId={}", newStatus, reportId, adminId);

        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        return contentReportRepository.countByStatus(ReportStatus.PENDING);
    }

    private void requirePlatformAdmin() {
        if (SecurityUtils.currentRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedResourceAccessException("Only platform admins can access the moderation queue");
        }
    }

    private ContentReportResponse toResponse(ContentReport report) {
        return new ContentReportResponse(report.getId(), report.getReportedBy(), report.getContentType(),
                report.getContentId(), report.getReason(), report.getDescription(), report.getStatus(),
                report.getCreatedAt());
    }
}

package com.yaxinaz.audit;

import com.yaxinaz.audit.dto.AuditLogResponse;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Tag(name = "Audit Log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/api/admin/audit-log")
    public ResponseEntity<PagedResponse<AuditLogResponse>> list(
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        if (SecurityUtils.currentRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedResourceAccessException("Only platform admins can view the audit log");
        }
        return ResponseEntity.ok(auditLogService.list(actionType, actorUserId, from, to, pageable));
    }
}

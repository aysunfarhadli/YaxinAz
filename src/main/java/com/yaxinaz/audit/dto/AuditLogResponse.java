package com.yaxinaz.audit.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actionType,
        String resourceType,
        Long resourceId,
        String oldValue,
        String newValue,
        String correlationId,
        Instant createdAt
) {
}

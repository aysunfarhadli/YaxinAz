package com.yaxinaz.health.dto;

import com.yaxinaz.audit.dto.AuditLogResponse;

import java.util.List;

public record OperationsCenterResponse(
        long criticalIssuesCount,
        long staleIssuesCount,
        long escalatedIssuesCount,
        long pendingMembershipsCount,
        long pendingProvidersCount,
        long moderationQueueCount,
        SystemHealthResponse systemHealth,
        List<AuditLogResponse> recentAuditTrail
) {
}

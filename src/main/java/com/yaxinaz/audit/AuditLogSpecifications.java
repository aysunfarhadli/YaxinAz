package com.yaxinaz.audit;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> hasActionType(AuditActionType actionType) {
        return (root, query, cb) -> actionType == null ? null : cb.equal(root.get("actionType"), actionType);
    }

    public static Specification<AuditLog> hasActor(Long actorUserId) {
        return (root, query, cb) -> actorUserId == null ? null : cb.equal(root.get("actorUserId"), actorUserId);
    }

    public static Specification<AuditLog> createdAfter(Instant from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<AuditLog> createdBefore(Instant to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}

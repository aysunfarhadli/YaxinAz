package com.yaxinaz.audit;

import com.yaxinaz.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Written synchronously, in the same transaction as the change it records (unlike Notification,
 * which is intentionally best-effort/after-commit). An audit trail that could silently disappear
 * because the enclosing operation happened to roll back is worse than useless.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    /** Null for system-initiated actions (e.g. the escalation scheduler). */
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditActionType actionType;

    @Column(nullable = false, length = 60)
    private String resourceType;

    private Long resourceId;

    @Column(length = 500)
    private String oldValue;

    @Column(length = 500)
    private String newValue;

    @Column(length = 60)
    private String correlationId;
}

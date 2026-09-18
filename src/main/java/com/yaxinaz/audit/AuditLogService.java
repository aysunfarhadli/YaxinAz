package com.yaxinaz.audit;

import com.yaxinaz.audit.dto.AuditLogResponse;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.util.CorrelationIdHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Called directly (never via an event) from within the same transaction as the change it
     * records - see the class-level note on {@link AuditLog}.
     */
    @Transactional
    public void record(Long actorUserId, AuditActionType actionType, String resourceType, Long resourceId,
                        String oldValue, String newValue) {
        auditLogRepository.save(AuditLog.builder()
                .actorUserId(actorUserId)
                .actionType(actionType)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .oldValue(oldValue)
                .newValue(newValue)
                .correlationId(CorrelationIdHolder.get())
                .build());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> list(AuditActionType actionType, Long actorUserId,
                                                 Instant from, Instant to, Pageable pageable) {
        Specification<AuditLog> spec = Specification.allOf(
                AuditLogSpecifications.hasActionType(actionType),
                AuditLogSpecifications.hasActor(actorUserId),
                AuditLogSpecifications.createdAfter(from),
                AuditLogSpecifications.createdBefore(to)
        );
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActorUserId(), log.getActionType().name(),
                log.getResourceType(), log.getResourceId(), log.getOldValue(), log.getNewValue(),
                log.getCorrelationId(), log.getCreatedAt());
    }
}

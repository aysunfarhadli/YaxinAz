package com.yaxinaz.ai;

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
 * Observability for AI calls (spec section 38). Deliberately does NOT store the raw user message,
 * API keys, or any auth material - only what's needed for an admin to see call volume/success rate/
 * latency and which provider actually served each request.
 */
@Entity
@Table(name = "ai_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIUsageLog extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String feature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AIProvider provider;

    @Column(length = 60)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AIUsageStatus status;

    private long latencyMs;
}

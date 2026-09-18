package com.yaxinaz.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface AIUsageLogRepository extends JpaRepository<AIUsageLog, Long> {

    long countByCreatedAtAfter(Instant since);

    long countByStatusAndCreatedAtAfter(AIUsageStatus status, Instant since);

    Optional<AIUsageLog> findTopByFeatureOrderByCreatedAtDesc(String feature);

    @Query("select avg(l.latencyMs) from AIUsageLog l where l.createdAt > :since")
    Double averageLatencyMsSince(Instant since);
}

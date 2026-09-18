package com.yaxinaz.health.dto;

import com.yaxinaz.ai.AIProvider;

import java.time.Instant;

public record SystemHealthResponse(
        String applicationStatus,
        String databaseStatus,
        AIProvider aiProvider,
        String webSocketStatus,
        String activeProfile,
        Instant lastCheckedAt
) {
}

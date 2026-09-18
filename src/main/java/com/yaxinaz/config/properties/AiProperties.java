package com.yaxinaz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yaxinaz.ai")
public record AiProperties(
        boolean enabled,
        boolean mockFallback,
        String model,
        long timeoutMs
) {
}

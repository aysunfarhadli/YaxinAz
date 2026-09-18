package com.yaxinaz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yaxinaz.rate-limit")
public record RateLimitProperties(
        int loginAttemptsPerMinute,
        int aiRequestsPerMinute,
        int issueCreationPerMinute,
        int announcementRewritePerMinute,
        int moderationReportPerMinute
) {
}

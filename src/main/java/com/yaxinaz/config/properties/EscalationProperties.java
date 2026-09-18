package com.yaxinaz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yaxinaz.escalation")
public record EscalationProperties(
        int criticalOpenMinutesThreshold,
        int highUnresolvedHoursThreshold,
        int staleIssueHoursThreshold
) {
}

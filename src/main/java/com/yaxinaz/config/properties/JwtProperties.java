package com.yaxinaz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yaxinaz.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        String issuer
) {
}

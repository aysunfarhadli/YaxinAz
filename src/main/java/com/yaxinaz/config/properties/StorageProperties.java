package com.yaxinaz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yaxinaz.storage")
public record StorageProperties(
        String basePath,
        long maxFileSizeBytes,
        java.util.List<String> allowedExtensions
) {
}

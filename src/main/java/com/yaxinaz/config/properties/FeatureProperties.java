package com.yaxinaz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yaxinaz.features")
public record FeatureProperties(
        boolean aiEnabled,
        boolean websocketEnabled,
        boolean naturalLanguageSearchEnabled,
        boolean mockAiFallbackEnabled,
        boolean fileUploadEnabled
) {
}

package com.yaxinaz.config;

import com.yaxinaz.config.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves files written by {@link com.yaxinaz.storage.FileStorageService} back out at
 * {@code /uploads/**}. The base dir must exist before {@code toUri()} is called, otherwise the
 * resulting URI lacks the trailing slash a resource location requires to resolve as a directory.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path baseDir = Path.of(storageProperties.basePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to create file storage directory: " + baseDir, ex);
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(baseDir.toUri().toString());
    }
}

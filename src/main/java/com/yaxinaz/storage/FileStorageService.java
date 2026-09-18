package com.yaxinaz.storage;

import com.yaxinaz.config.properties.FeatureProperties;
import com.yaxinaz.config.properties.StorageProperties;
import com.yaxinaz.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;
    private final FeatureProperties featureProperties;

    public FileUploadResponse store(MultipartFile file) {
        if (!featureProperties.fileUploadEnabled()) {
            throw new InvalidFileException("File uploads are currently disabled.");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("No file was provided.");
        }
        if (file.getSize() > storageProperties.maxFileSizeBytes()) {
            throw new InvalidFileException("File exceeds the maximum allowed size of "
                    + (storageProperties.maxFileSizeBytes() / (1024 * 1024)) + "MB.");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (extension.isEmpty() || !storageProperties.allowedExtensions().contains(extension)) {
            throw new InvalidFileException("Unsupported file type. Allowed types: "
                    + String.join(", ", storageProperties.allowedExtensions()));
        }

        Path baseDir = Path.of(storageProperties.basePath()).toAbsolutePath().normalize();
        String storedFilename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(baseDir);
            file.transferTo(baseDir.resolve(storedFilename));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store uploaded file", ex);
        }

        return new FileUploadResponse("/uploads/" + storedFilename, storedFilename, file.getSize(), file.getContentType());
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

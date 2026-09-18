package com.yaxinaz.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        Map<String, String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, error, message, path, correlationId, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path,
                                               String correlationId, Map<String, String> validationErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, correlationId, validationErrors);
    }
}

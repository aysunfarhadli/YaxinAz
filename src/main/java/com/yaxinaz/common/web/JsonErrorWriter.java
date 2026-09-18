package com.yaxinaz.common.web;

import com.yaxinaz.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

/**
 * Spring Security's entry point / access-denied handlers run outside the normal MVC dispatch path,
 * so they can't rely on the framework's auto-configured Jackson message converter bean (Spring Boot 4
 * carries both a Jackson 2 and a Jackson 3 ObjectMapper on the classpath, and only one is wired as a
 * bean depending on autoconfiguration order - see progress.md). Writing the small fixed error body by
 * hand avoids that ambiguity entirely.
 */
public final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    public static void write(HttpServletResponse response, HttpStatus status, String message, String path, String correlationId)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, path, correlationId);
        String json = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s","correlationId":"%s"}"""
                .formatted(
                        body.timestamp(),
                        body.status(),
                        escape(body.error()),
                        escape(body.message()),
                        escape(body.path()),
                        escape(body.correlationId())
                );
        response.getWriter().write(json);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}

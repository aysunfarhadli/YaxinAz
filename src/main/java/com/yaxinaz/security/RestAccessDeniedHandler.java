package com.yaxinaz.security;

import com.yaxinaz.common.web.JsonErrorWriter;
import com.yaxinaz.util.CorrelationIdHolder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        JsonErrorWriter.write(
                response,
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.",
                request.getRequestURI(),
                CorrelationIdHolder.get()
        );
    }
}

package com.yaxinaz.security;

import com.yaxinaz.common.web.JsonErrorWriter;
import com.yaxinaz.util.CorrelationIdHolder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        JsonErrorWriter.write(
                response,
                HttpStatus.UNAUTHORIZED,
                "Your session has expired or is invalid. Please sign in again.",
                request.getRequestURI(),
                CorrelationIdHolder.get()
        );
    }
}

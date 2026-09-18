package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class AIServiceException extends ApiException {
    public AIServiceException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }

    public AIServiceException(String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, message);
        initCause(cause);
    }
}

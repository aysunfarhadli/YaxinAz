package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedResourceAccessException extends ApiException {
    public UnauthorizedResourceAccessException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}

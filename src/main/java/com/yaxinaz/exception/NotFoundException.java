package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    protected NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

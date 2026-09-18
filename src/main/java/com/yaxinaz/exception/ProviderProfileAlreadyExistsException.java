package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class ProviderProfileAlreadyExistsException extends ApiException {
    public ProviderProfileAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

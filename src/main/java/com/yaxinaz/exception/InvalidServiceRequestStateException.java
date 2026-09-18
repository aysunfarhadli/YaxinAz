package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class InvalidServiceRequestStateException extends ApiException {
    public InvalidServiceRequestStateException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

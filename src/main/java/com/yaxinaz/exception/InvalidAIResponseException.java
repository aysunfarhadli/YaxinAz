package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class InvalidAIResponseException extends ApiException {
    public InvalidAIResponseException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}

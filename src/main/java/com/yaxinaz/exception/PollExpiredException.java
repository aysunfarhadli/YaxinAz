package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class PollExpiredException extends ApiException {
    public PollExpiredException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

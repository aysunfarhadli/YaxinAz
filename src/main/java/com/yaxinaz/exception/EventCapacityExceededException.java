package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class EventCapacityExceededException extends ApiException {
    public EventCapacityExceededException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

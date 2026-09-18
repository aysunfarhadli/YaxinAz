package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class InvalidIssueStatusTransitionException extends ApiException {
    public InvalidIssueStatusTransitionException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}

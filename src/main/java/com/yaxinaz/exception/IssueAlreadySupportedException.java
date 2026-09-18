package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class IssueAlreadySupportedException extends ApiException {
    public IssueAlreadySupportedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

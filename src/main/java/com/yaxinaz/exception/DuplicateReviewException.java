package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class DuplicateReviewException extends ApiException {
    public DuplicateReviewException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockConflictException extends ApiException {
    public OptimisticLockConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

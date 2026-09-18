package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class DuplicateMembershipException extends ApiException {
    public DuplicateMembershipException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

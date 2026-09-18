package com.yaxinaz.exception;

import org.springframework.http.HttpStatus;

public class AlreadyVotedException extends ApiException {
    public AlreadyVotedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}

package com.yaxinaz.exception;

public class PollNotFoundException extends NotFoundException {
    public PollNotFoundException(Long id) {
        super("Poll not found: id=" + id);
    }
}

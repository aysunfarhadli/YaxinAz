package com.yaxinaz.exception;

public class IssueNotFoundException extends NotFoundException {
    public IssueNotFoundException(Long id) {
        super("Issue not found: id=" + id);
    }
}

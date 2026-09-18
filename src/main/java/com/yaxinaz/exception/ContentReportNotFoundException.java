package com.yaxinaz.exception;

public class ContentReportNotFoundException extends NotFoundException {
    public ContentReportNotFoundException(Long id) {
        super("Content report not found: id=" + id);
    }
}

package com.yaxinaz.exception;

public class ProviderNotFoundException extends NotFoundException {
    public ProviderNotFoundException(Long id) {
        super("Service provider not found: id=" + id);
    }
}

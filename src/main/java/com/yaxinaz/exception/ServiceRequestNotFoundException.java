package com.yaxinaz.exception;

public class ServiceRequestNotFoundException extends NotFoundException {
    public ServiceRequestNotFoundException(Long id) {
        super("Service request not found: id=" + id);
    }
}

package com.yaxinaz.exception;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(Long id) {
        super("Event not found: id=" + id);
    }
}

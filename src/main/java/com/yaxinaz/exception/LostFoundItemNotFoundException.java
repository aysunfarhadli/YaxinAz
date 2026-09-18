package com.yaxinaz.exception;

public class LostFoundItemNotFoundException extends NotFoundException {
    public LostFoundItemNotFoundException(Long id) {
        super("Lost & found item not found: id=" + id);
    }
}

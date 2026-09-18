package com.yaxinaz.exception;

public class PostNotFoundException extends NotFoundException {
    public PostNotFoundException(Long id) {
        super("Post not found: id=" + id);
    }
}

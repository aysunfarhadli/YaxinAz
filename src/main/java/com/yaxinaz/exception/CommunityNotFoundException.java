package com.yaxinaz.exception;

public class CommunityNotFoundException extends NotFoundException {
    public CommunityNotFoundException(Long id) {
        super("Community not found: id=" + id);
    }
}

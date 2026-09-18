package com.yaxinaz.exception;

public class UserNotFoundException extends NotFoundException {

    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byId(Long id) {
        return new UserNotFoundException("User not found: id=" + id);
    }

    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("User not found: email=" + email);
    }
}

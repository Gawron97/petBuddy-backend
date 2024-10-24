package com.example.petbuddybackend.utils.exception.throweable;

import com.example.petbuddybackend.entity.user.Role;

public class InvalidRoleException extends RuntimeException {

    private static final String INVALID_ROLE_MESSAGE = "User does not have the required role. Expected: %s";

    public InvalidRoleException(Role expectedRole) {
        super(String.format(INVALID_ROLE_MESSAGE, expectedRole));
    }
}

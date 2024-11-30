package com.example.petbuddybackend.utils.exception.throweable.user;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class InvalidRoleException extends HttpException {

    private static final String INVALID_ROLE_MESSAGE = "User does not have the required role: %s";

    public InvalidRoleException(Role expectedRole) {
        super(String.format(INVALID_ROLE_MESSAGE, expectedRole), HttpStatus.FORBIDDEN);
    }
}

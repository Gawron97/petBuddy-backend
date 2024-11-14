package com.example.petbuddybackend.utils.exception.throweable.general;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends HttpException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

}

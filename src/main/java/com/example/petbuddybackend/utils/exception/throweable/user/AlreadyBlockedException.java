package com.example.petbuddybackend.utils.exception.throweable.user;


import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class AlreadyBlockedException extends HttpException {

    public AlreadyBlockedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

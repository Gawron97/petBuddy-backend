package com.example.petbuddybackend.utils.exception.throweable.general;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class IllegalActionException extends HttpException {

    public IllegalActionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}

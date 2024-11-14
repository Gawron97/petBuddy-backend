package com.example.petbuddybackend.utils.exception.throweable.chat;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class InvalidMessageReceiverException extends HttpException {

    public InvalidMessageReceiverException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

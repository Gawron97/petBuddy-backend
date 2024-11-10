package com.example.petbuddybackend.utils.exception.throweable.websocket;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class InvalidWebSocketHeaderException extends HttpException {

    public InvalidWebSocketHeaderException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

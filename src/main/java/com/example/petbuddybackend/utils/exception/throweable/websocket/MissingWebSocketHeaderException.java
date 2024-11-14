package com.example.petbuddybackend.utils.exception.throweable.websocket;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class MissingWebSocketHeaderException extends HttpException {

    public MissingWebSocketHeaderException(String headerName) {
        super("Missing required header: " + headerName, HttpStatus.BAD_REQUEST);
    }
}

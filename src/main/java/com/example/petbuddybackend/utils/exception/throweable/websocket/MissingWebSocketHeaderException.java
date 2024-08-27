package com.example.petbuddybackend.utils.exception.throweable.websocket;

public class MissingWebSocketHeaderException extends RuntimeException {

    public MissingWebSocketHeaderException(String headerName) {
        super("Missing required header: " + headerName);
    }
}

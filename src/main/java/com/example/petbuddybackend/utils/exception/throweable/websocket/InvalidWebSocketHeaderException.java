package com.example.petbuddybackend.utils.exception.throweable.websocket;

public class InvalidWebSocketHeaderException extends RuntimeException {

    public InvalidWebSocketHeaderException(String message) {
        super(message);
    }
}

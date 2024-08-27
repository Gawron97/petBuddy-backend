package com.example.petbuddybackend.utils.exception.throweable.chat;

public class InvalidMessageReceiverException extends RuntimeException {

    public InvalidMessageReceiverException(String message) {
        super(message);
    }
}

package com.example.petbuddybackend.utils.exception.throweable.chat;

public class ChatAlreadyExistsException extends RuntimeException {

    public ChatAlreadyExistsException(String message) {
        super(message);
    }
}

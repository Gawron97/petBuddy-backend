package com.example.petbuddybackend.utils.exception.throweable;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String message) {
        super(message);
    }
}

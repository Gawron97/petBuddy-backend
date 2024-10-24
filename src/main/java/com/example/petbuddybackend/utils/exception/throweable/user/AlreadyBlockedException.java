package com.example.petbuddybackend.utils.exception.throweable.user;


public class AlreadyBlockedException extends RuntimeException {

    public AlreadyBlockedException(String message) {
        super(message);
    }
}

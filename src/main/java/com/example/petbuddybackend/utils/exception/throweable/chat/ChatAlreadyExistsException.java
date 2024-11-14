package com.example.petbuddybackend.utils.exception.throweable.chat;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class ChatAlreadyExistsException extends HttpException {

    public ChatAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

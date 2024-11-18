package com.example.petbuddybackend.utils.exception.throweable.chat;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class NotParticipateException extends HttpException {

    public NotParticipateException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

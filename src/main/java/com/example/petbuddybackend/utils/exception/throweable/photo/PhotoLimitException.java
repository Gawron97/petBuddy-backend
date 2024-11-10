package com.example.petbuddybackend.utils.exception.throweable.photo;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class PhotoLimitException extends HttpException {

    public PhotoLimitException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

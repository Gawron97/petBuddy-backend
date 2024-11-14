package com.example.petbuddybackend.utils.exception.throweable.offer;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class AvailabilityDatesOverlappingException extends HttpException {

    public AvailabilityDatesOverlappingException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}

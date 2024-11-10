package com.example.petbuddybackend.utils.exception.throweable.offer;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;

public class OfferConfigurationDuplicatedException extends HttpException {

    public OfferConfigurationDuplicatedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}

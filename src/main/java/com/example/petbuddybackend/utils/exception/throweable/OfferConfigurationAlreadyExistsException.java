package com.example.petbuddybackend.utils.exception.throweable;

public class OfferConfigurationAlreadyExistsException extends RuntimeException {

    public OfferConfigurationAlreadyExistsException(String message) {
        super(message);
    }

    public OfferConfigurationAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}

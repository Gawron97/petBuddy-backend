package com.example.petbuddybackend.utils.exception.throweable.offer;

public class OfferConfigurationDuplicatedException extends RuntimeException {

    public OfferConfigurationDuplicatedException(String message) {
        super(message);
    }

    public OfferConfigurationDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }

}

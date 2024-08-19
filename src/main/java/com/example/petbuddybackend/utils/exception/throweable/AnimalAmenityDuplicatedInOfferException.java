package com.example.petbuddybackend.utils.exception.throweable;

public class AnimalAmenityDuplicatedInOfferException extends RuntimeException {

    public AnimalAmenityDuplicatedInOfferException(String message) {
        super(message);
    }

    public AnimalAmenityDuplicatedInOfferException(String message, Throwable cause) {
        super(message, cause);
    }
}

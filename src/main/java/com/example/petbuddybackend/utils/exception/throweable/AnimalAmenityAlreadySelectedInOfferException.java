package com.example.petbuddybackend.utils.exception.throweable;

public class AnimalAmenityAlreadySelectedInOfferException extends RuntimeException {

    public AnimalAmenityAlreadySelectedInOfferException(String message) {
        super(message);
    }

    public AnimalAmenityAlreadySelectedInOfferException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.example.petbuddybackend.utils.exception.user;

public class CaretakerNotFoundException extends RuntimeException {

    public CaretakerNotFoundException(String email) {
        super("Caretaker with email " + email + " not found");
    }

}

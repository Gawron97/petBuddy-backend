package com.example.petbuddybackend.utils.exception.throweable.photo;

import java.util.Set;

public class InvalidPhotoException extends RuntimeException {

    private static final String INVALID_FILE_TYPE_MESSAGE =
            "Invalid file type. Provided: %s, allowed: %s";

    public InvalidPhotoException(String message) {
        super(message);
    }

    public static InvalidPhotoException invalidType(String extension, Set<String> allowedExtensions) {
        String extensions = allowedExtensions.stream()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return new InvalidPhotoException(String.format(INVALID_FILE_TYPE_MESSAGE, extension, extensions));
    }
}

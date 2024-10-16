package com.example.petbuddybackend.utils.exception.throweable.photo;

import java.util.Set;

public class InvalidPhotoException extends RuntimeException {

    private static final String EMPTY_MULTIPART_FILE_MESSAGE = "The multipart file is empty";
    private static final String INVALID_PHOTO_PROVIDED_MESSAGE = "Invalid photo provided: %s";
    private static final String INVALID_FILE_TYPE_MESSAGE =
            "Invalid file type for file: %s. Provided: %s, allowed: %s";

    public InvalidPhotoException(String message) {
        super(message);
    }

    public static InvalidPhotoException ofPhotoWithInvalidExtension(
            String filename,
            String extension,
            Set<String> allowedExtensions
    ) {
        String extensions = allowedExtensions.stream()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return new InvalidPhotoException(String.format(INVALID_FILE_TYPE_MESSAGE, filename, extension, extensions));
    }

    public static InvalidPhotoException ofInvalidPhoto(String filename) {
        return new InvalidPhotoException(String.format(INVALID_PHOTO_PROVIDED_MESSAGE, filename));
    }

    public static InvalidPhotoException ofEmptyPhoto() {
        return new InvalidPhotoException(EMPTY_MULTIPART_FILE_MESSAGE);
    }
}

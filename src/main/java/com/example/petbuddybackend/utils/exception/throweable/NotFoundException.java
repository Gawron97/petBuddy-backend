package com.example.petbuddybackend.utils.exception.throweable;

public class NotFoundException extends RuntimeException {

    private static final String TYPED_RESOURCE_NOT_FOUND_MESSAGE = "\"%s\" of name \"%s\" not found";

    public NotFoundException() {
        super("Resource not found");
    }

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException withFormattedMessage(String name, String resourceTypeName)
    {
        return new NotFoundException(String.format(TYPED_RESOURCE_NOT_FOUND_MESSAGE, resourceTypeName, name));
    }
}

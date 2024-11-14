package com.example.petbuddybackend.utils.exception.throweable.general;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class NotFoundException extends HttpException {

    private static final HttpStatusCode HTTP_STATUS = HttpStatus.NOT_FOUND;
    private static final String TYPED_RESOURCE_NOT_FOUND_MESSAGE = "\"%s\" :: \"%s\" not found";

    public NotFoundException() {
        super("Resource not found", HTTP_STATUS);
    }

    public NotFoundException(String message) {
        super(message, HTTP_STATUS);
    }

    public static NotFoundException withFormattedMessage(String resourceTypeName, String name)
    {
        return new NotFoundException(String.format(TYPED_RESOURCE_NOT_FOUND_MESSAGE, resourceTypeName, name));
    }
}

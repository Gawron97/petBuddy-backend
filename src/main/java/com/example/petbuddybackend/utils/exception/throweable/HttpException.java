package com.example.petbuddybackend.utils.exception.throweable;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class HttpException extends RuntimeException {

    private final HttpStatusCode code;

    public HttpException(String message, HttpStatusCode code) {
        super(message);
        this.code = code;
    }
}

package com.example.petbuddybackend.utils.exception;

import com.example.petbuddybackend.utils.time.TimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ApiExceptionResponse {

    String type;
    String message;

    @JsonFormat(pattern = TimeFormat.DATE_TIME_FORMAT)
    LocalDateTime timestamp;

    public ApiExceptionResponse(Throwable e, String message) {
        this.type = e.getClass().getSimpleName();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ApiExceptionResponse(Throwable e) {
        this(e, "An unexpected error occurred");
    }
}

package com.example.petbuddybackend.utils.exception;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiExceptionResponse {

    private String type;
    private String message;
    private int code;

    @JsonFormat(pattern = TimeUtils.DATE_TIME_FORMAT)
    private LocalDateTime timestamp;

    public ApiExceptionResponse(Throwable e, String message, HttpStatusCode code) {
        this.type = e.getClass().getSimpleName();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.code = code.value();
    }

    public ApiExceptionResponse(HttpException e, String message) {
        this.type = e.getClass().getSimpleName();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.code = e.getCode().value();
    }

    public ApiExceptionResponse(Throwable e) {
        this(e, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

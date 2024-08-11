package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionResponse handleException(Throwable t) {
        log.error("Unhandled exception", t);
        return new ApiExceptionResponse(t);
    }
}

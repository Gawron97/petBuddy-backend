package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GeneralAdvice {

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiExceptionResponse handleException(Throwable t) {
        log.error("Unhandled exception", t);
        return new ApiExceptionResponse(t);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.toString(error.getDefaultMessage(), "Invalid value"),
                        (existing, replacement) -> existing
                ));

        return new ApiExceptionResponse(e, errors.toString());
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ApiExceptionResponse handleNotFoundException(NotFoundException e) {
        return new ApiExceptionResponse(e);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(IllegalActionException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleIllegalActionException(IllegalActionException e) {
        return new ApiExceptionResponse(e);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handlePropertyReferenceException(PropertyReferenceException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }
}

package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.service.chat.ResourceAlreadyExists;
import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.NotParticipateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeneralAdvice {

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

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public ApiExceptionResponse handleNotFoundException(NotFoundException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(IllegalActionException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleIllegalActionException(IllegalActionException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handlePropertyReferenceException(PropertyReferenceException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(NotParticipateException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ApiExceptionResponse handleNotParticipantException(NotParticipateException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(ZoneRulesException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleZoneRulesException(ZoneRulesException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(DateTimeException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleDateTimeException(DateTimeException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExists.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleResourceAlreadyExists(ResourceAlreadyExists e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        return new ApiExceptionResponse(e, "Required header missing");
    }
}

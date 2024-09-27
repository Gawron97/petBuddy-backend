package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.throweable.general.DateRangeException;
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import com.example.petbuddybackend.utils.exception.throweable.photo.InvalidPhotoException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.chat.ChatAlreadyExistsException;
import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.chat.InvalidMessageReceiverException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        var errorList = e.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> {
                            if (error instanceof FieldError fieldError) {
                                return result.getMethodParameter().getParameterName() + "." + fieldError.getField() + ": " + fieldError.getDefaultMessage();
                            }
                            return result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage();
                        }))
                .toList();

        return new ApiExceptionResponse(e, errorList.toString());
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

    @ExceptionHandler(ChatAlreadyExistsException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleResourceAlreadyExists(ChatAlreadyExistsException e) {
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(InvalidMessageReceiverException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleInvalidMessageReceiverException(InvalidMessageReceiverException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(MissingWebSocketHeaderException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMissingWebSocketHeaderException(MissingWebSocketHeaderException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(InvalidWebSocketHeaderException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleInvalidWebSocketHeaderException(InvalidWebSocketHeaderException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ApiExceptionResponse handleUnauthorizedException(UnauthorizedException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(DateRangeException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleDateRangeException(DateRangeException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPhotoException.class)
    public ApiExceptionResponse handleGeneralException(InvalidPhotoException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }
}

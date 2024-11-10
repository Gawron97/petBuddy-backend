package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.common.ExceptionUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GeneralAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.debug("MethodArgumentNotValidException", e);
        return new ApiExceptionResponse(e, ExceptionUtils.getInvalidFields(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.debug("HandlerMethodValidationException", e);
        return new ApiExceptionResponse(e, ExceptionUtils.getInvalidFields(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handlePropertyReferenceException(PropertyReferenceException e) {
        log.debug("PropertyReferenceException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ZoneRulesException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleZoneRulesException(ZoneRulesException e) {
        log.debug("ZoneRulesException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleDateTimeException(DateTimeException e) {
        log.debug("DateTimeException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.debug("HttpMessageNotReadableException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.debug("MissingRequestHeaderException", e);
        return new ApiExceptionResponse(e, "Required header missing", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.debug("MethodArgumentTypeMismatchException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(code = HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiExceptionResponse handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.debug("MaxUploadSizeExceededException", e);
        return new ApiExceptionResponse(e, "Maximum file upload size exceeded", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ApiExceptionResponse handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.debug("AuthorizationDeniedException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.debug("NoHandlerFoundException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.debug("MissingServletRequestPartException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiExceptionResponse handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.debug("HttpMediaTypeNotSupportedException", e);
        return new ApiExceptionResponse(e, e.getBody().getDetail(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
    public ApiExceptionResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.debug("HttpRequestMethodNotSupportedException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ApiExceptionResponse handleForbiddenException(ForbiddenException e) {
        log.debug("ForbiddenException", e);
        return new ApiExceptionResponse(e, e.getMessage(), HttpStatus.FORBIDDEN);
    }
}

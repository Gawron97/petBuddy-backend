package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.chat.ChatAlreadyExistsException;
import com.example.petbuddybackend.utils.exception.throweable.chat.InvalidMessageReceiverException;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import com.example.petbuddybackend.utils.exception.throweable.general.DateRangeException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import com.example.petbuddybackend.utils.exception.throweable.photo.InvalidPhotoException;
import com.example.petbuddybackend.utils.exception.throweable.user.AlreadyBlockedException;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomExceptionAdvice {

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

    @ExceptionHandler(NotParticipateException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ApiExceptionResponse handleNotParticipantException(NotParticipateException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleResourceAlreadyExists(ChatAlreadyExistsException e) {
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

    @ExceptionHandler(InvalidPhotoException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleGeneralException(InvalidPhotoException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(DateRangeException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse handleDateRangeException(DateRangeException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ApiExceptionResponse handleUnauthorizedException(UnauthorizedException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(AlreadyBlockedException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleBlockActionAlreadyPerformed(AlreadyBlockedException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(BlockedException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ApiExceptionResponse handleBlockedException(BlockedException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }
}

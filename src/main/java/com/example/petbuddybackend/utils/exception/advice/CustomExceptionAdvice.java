package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomExceptionAdvice {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ApiExceptionResponse> handleNotFoundException(HttpException e) {
        log.debug("Handling HttpException: {}", e.getMessage());

        return ResponseEntity
                .status(e.getCode())
                .body(new ApiExceptionResponse(e, e.getMessage()));
    }
}

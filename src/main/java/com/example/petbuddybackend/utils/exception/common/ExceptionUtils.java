package com.example.petbuddybackend.utils.exception.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionUtils {

    public static String getInvalidFields(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.toString(error.getDefaultMessage(), "Invalid value"),
                        (existing, replacement) -> existing
                ))
                .toString();
    }

    public static String getInvalidFields(HandlerMethodValidationException e) {
        return e.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> {
                            if (error instanceof FieldError fieldError) {
                                return result.getMethodParameter().getParameterName() + "." + fieldError.getField() +
                                        ": " + fieldError.getDefaultMessage();
                            }
                            return result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage();
                        }))
                .toList()
                .toString();
    }
}

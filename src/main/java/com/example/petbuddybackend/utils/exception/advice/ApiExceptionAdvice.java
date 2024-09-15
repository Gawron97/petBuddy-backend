package com.example.petbuddybackend.utils.exception.advice;

import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.offer.AnimalAmenityDuplicatedInOfferException;
import com.example.petbuddybackend.utils.exception.throweable.offer.AvailabilityDatesOverlappingException;
import com.example.petbuddybackend.utils.exception.throweable.offer.OfferConfigurationDuplicatedException;
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

    @ExceptionHandler(OfferConfigurationDuplicatedException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleOfferConfigurationAlreadyExistsException(OfferConfigurationDuplicatedException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(AnimalAmenityDuplicatedInOfferException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleAnimalAmenityAlreadySelectedInOfferException(AnimalAmenityDuplicatedInOfferException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

    @ExceptionHandler(AvailabilityDatesOverlappingException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public ApiExceptionResponse handleAvailabilityDatesOverlappingException(AvailabilityDatesOverlappingException e) {
        return new ApiExceptionResponse(e, e.getMessage());
    }

}

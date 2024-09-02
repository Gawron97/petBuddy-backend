package com.example.petbuddybackend.utils.annotation.validation;

import com.example.petbuddybackend.utils.exception.throweable.general.DateRangeException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.annotation.Annotation;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateRangeValidatorTest {

    private DateRangeValidator dateRangeValidator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dateRangeValidator = new DateRangeValidator();
        dateRangeValidator.initialize(new DateRange() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DateRange.class;
            }

            @Override
            public String message() {
                return "Invalid date range custom message";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public String startDateField() {
                return "startDate";
            }

            @Override
            public String endDateField() {
                return "endDate";
            }
        });
    }

    @Test
    void testValidDateRange_whenDateRangeCorrect_ShouldReturnTrue() {

        // Given
        TestObject testObject = TestObject.builder()
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2021, 1, 2))
                .build();

        // When
        boolean result = dateRangeValidator.isValid(testObject, context);

        // Then
        assertTrue(result);

    }

    @Test
    void testValidDateRange_whenDateRangeInCorrect_ShouldThrowDateRangeExcpetion() {

        // Given
        TestObject testObject = TestObject.builder()
                .startDate(LocalDate.of(2021, 1, 10))
                .endDate(LocalDate.of(2021, 1, 2))
                .build();

        // When Then
        assertThrows(DateRangeException.class, () -> dateRangeValidator.isValid(testObject, context));

    }

    @Test
    void testValidDateRange_whenNotProperFields_ShouldThrowIllegalActionException() {

        // Given
        InvalidTestObject testObject = InvalidTestObject.builder()
                .startDate("2021-01-01")
                .endDate("2021-01-02")
                .build();

        // When Then
        assertThrows(IllegalActionException.class, () -> dateRangeValidator.isValid(testObject, context));

    }

    @Builder
    static class TestObject {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Builder
    static class InvalidTestObject {
        private String startDate;
        private String endDate;
    }

}

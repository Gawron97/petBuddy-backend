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
    }

    @Test
    void testValidDateRange_whenDateRangeCorrect_ShouldReturnTrue() {

        // Given
        initializeDateRangeValidator(initializeDateRangeFieldsWithOnePair());
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
    void testValidDateRange_whenDateRangeInCorrect_ShouldThrowDateRangeException() {

        // Given
        initializeDateRangeValidator(initializeDateRangeFieldsWithOnePair());
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
        initializeDateRangeValidator(initializeDateRangeFieldsWithOnePair());
        InvalidTestObject testObject = InvalidTestObject.builder()
                .startDate("2021-01-01")
                .endDate("2021-01-02")
                .build();

        // When Then
        assertThrows(IllegalActionException.class, () -> dateRangeValidator.isValid(testObject, context));

    }

    @Test
    void testValidDateRange_WhenMoreThenOnePairOfFieldsAndAllValid_ShouldReturnTrue() {

        // Given
        initializeDateRangeValidator(initializeDateRangeFieldsWithTwoPairs());
        MultipleFieldsTestObject multipleFieldsTestObject = MultipleFieldsTestObject.builder()
                .startDate(LocalDate.of(2021, 2, 1))
                .endDate(LocalDate.of(2021, 2, 2))
                .startDate2(LocalDate.of(2021, 1, 3))
                .endDate2(LocalDate.of(2021, 1, 4))
                .build();

        // When
        boolean result = dateRangeValidator.isValid(multipleFieldsTestObject, context);

        // Then
        assertTrue(result);

    }

    @Test
    void testValidDateRange_WhenMoreThenOnePairOfFieldsAndOneInValid_ShouldThrowDateRangeException() {

        // Given
        initializeDateRangeValidator(initializeDateRangeFieldsWithTwoPairs());
        MultipleFieldsTestObject multipleFieldsTestObject = MultipleFieldsTestObject.builder()
                .startDate(LocalDate.of(2021, 2, 1))
                .endDate(LocalDate.of(2021, 2, 2))
                .startDate2(LocalDate.of(2021, 1, 3))
                .endDate2(LocalDate.of(2021, 1, 1))
                .build();

        // When Then
        assertThrows(DateRangeException.class, () -> dateRangeValidator.isValid(multipleFieldsTestObject, context));

    }

    @Test
    void testValidDateRange_WhenMoreThenOnePairOfFieldsAndOneInNotProperDate_ShouldThrowIllegalActionException() {

        // Given
        initializeDateRangeValidator(initializeDateRangeFieldsWithTwoPairs());
        MultipleFieldsInvalidTestObject multipleFieldsTestObject = MultipleFieldsInvalidTestObject.builder()
                .startDate("2021-02-01")
                .endDate("2021-02-02")
                .startDate2("2021-01-03")
                .endDate2("2021-01-01")
                .build();

        // When Then
        assertThrows(IllegalActionException.class, () -> dateRangeValidator.isValid(multipleFieldsTestObject, context));

    }

    private DateRangeField[] initializeDateRangeFieldsWithOnePair() {
        return new DateRangeField[] {
                new DateRangeField() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return DateRangeField.class;
                    }

                    @Override
                    public String startDateField() {
                        return "startDate";
                    }

                    @Override
                    public String endDateField() {
                        return "endDate";
                    }
                }
        };
    }

    private DateRangeField[] initializeDateRangeFieldsWithTwoPairs() {
        return new DateRangeField[] {
                new DateRangeField() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return DateRangeField.class;
                    }

                    @Override
                    public String startDateField() {
                        return "startDate";
                    }

                    @Override
                    public String endDateField() {
                        return "endDate";
                    }
                },
                new DateRangeField() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return DateRangeField.class;
                    }

                    @Override
                    public String startDateField() {
                        return "startDate2";
                    }

                    @Override
                    public String endDateField() {
                        return "endDate2";
                    }
                }
        };
    }

    private void initializeDateRangeValidator(DateRangeField[] dateRangeFields) {
        dateRangeValidator = new DateRangeValidator();
        DateRangeField dateRangeField = new DateRangeField() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DateRangeField.class;
            }

            @Override
            public String startDateField() {
                return "startDate";
            }

            @Override
            public String endDateField() {
                return "endDate";
            }
        };

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
            public DateRangeField[] fields() {
                return dateRangeFields;
            }
        });
    }

    @Builder
    static class MultipleFieldsTestObject {
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate startDate2;
        private LocalDate endDate2;
    }

    @Builder
    static class TestObject {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Builder
    static class MultipleFieldsInvalidTestObject {
        private String startDate;
        private String endDate;
        private String startDate2;
        private String endDate2;
    }


    @Builder
    static class InvalidTestObject {
        private String startDate;
        private String endDate;
    }

}

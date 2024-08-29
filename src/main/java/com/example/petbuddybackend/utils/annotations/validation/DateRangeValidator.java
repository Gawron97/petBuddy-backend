package com.example.petbuddybackend.utils.annotations.validation;

import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {

    private String startDateFieldName;
    private String endDateFieldName;
    private String message;

    @Override
    public void initialize(DateRange constraintAnnotation) {
        startDateFieldName = constraintAnnotation.startDateField();
        endDateFieldName = constraintAnnotation.endDateField();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        try {
            Field startDateField = value.getClass().getDeclaredField(this.startDateFieldName);
            Field endDateField = value.getClass().getDeclaredField(this.endDateFieldName);
            startDateField.setAccessible(true);
            endDateField.setAccessible(true);

            Object startDate = startDateField.get(value);
            Object endDate = endDateField.get(value);

            if(startDate == null || endDate == null) {
                return true;
            }

            if(!(startDate instanceof Temporal) || !(endDate instanceof Temporal) ||
                    !(startDate instanceof Comparable) || !(endDate instanceof Comparable)) {
                throw new IllegalActionException("DateRangeValidator can only be used on Date fields implementing Comparable");
            }

            Comparable<Object> startDateComparable = (Comparable<Object>) startDate;
            Comparable<Object> endDateComparable = (Comparable<Object>) endDate;

            boolean isValid = startDateComparable.compareTo(endDateComparable) < 0;

            if (!isValid) {
                // Disable default constraint violation and add a custom message
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(value.getClass().getSimpleName())
                        .addConstraintViolation();
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }

    }
}

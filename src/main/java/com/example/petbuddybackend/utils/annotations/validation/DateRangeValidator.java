package com.example.petbuddybackend.utils.annotations.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

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

            Comparable<Object> startDate = (Comparable<Object>) startDateField.get(value);
            Comparable<Object> endDate = (Comparable<Object>) endDateField.get(value);

            if(startDate == null || endDate == null) {
                return true;
            }

            boolean isValid = startDate.compareTo(endDate) < 0;

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

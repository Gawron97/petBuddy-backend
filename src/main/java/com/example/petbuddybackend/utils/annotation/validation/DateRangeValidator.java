package com.example.petbuddybackend.utils.annotation.validation;

import com.example.petbuddybackend.utils.exception.throweable.general.DateRangeException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
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
    public boolean isValid(Object value, ConstraintValidatorContext context) throws DateRangeException {

        boolean isValid;

        try {
            Field startDateField = value.getClass().getDeclaredField(this.startDateFieldName);
            Field endDateField = value.getClass().getDeclaredField(this.endDateFieldName);
            startDateField.setAccessible(true);
            endDateField.setAccessible(true);

            Object startDate = startDateField.get(value);
            Object endDate = endDateField.get(value);

            isValid = isValid(startDate, endDate);

        } catch (IllegalActionException e) {
            throw e;
        }
        catch (Exception e) {
            return false;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            throw new DateRangeException(message);
        }

        return isValid;

    }

    private static boolean isValid(Object startDate, Object endDate) throws IllegalActionException {
        if(startDate == null || endDate == null) {
            return true;
        }

        if(!(startDate instanceof Temporal) || !(endDate instanceof Temporal) ||
                !(startDate instanceof Comparable) || !(endDate instanceof Comparable)) {
            throw new IllegalActionException("DateRangeValidator can only be used on Date fields implementing Comparable");
        }

        Comparable<Object> startDateComparable = (Comparable<Object>) startDate;
        Comparable<Object> endDateComparable = (Comparable<Object>) endDate;

        return startDateComparable.compareTo(endDateComparable) < 0;
    }


}

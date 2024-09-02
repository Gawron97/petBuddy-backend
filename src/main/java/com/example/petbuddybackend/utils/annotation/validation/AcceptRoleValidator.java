package com.example.petbuddybackend.utils.annotation.validation;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class AcceptRoleValidator implements ConstraintValidator<AcceptRole, Role> {

    private Role[] acceptRoles;
    private String message;

    @Override
    public void initialize(AcceptRole constraintAnnotation) {
        acceptRoles = constraintAnnotation.acceptRole();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Role value, ConstraintValidatorContext context) {

        if(!isValid(value, acceptRoles)) {
            context.disableDefaultConstraintViolation();
            throw new UnauthorizedException(message);
        }
        return true;

    }

    private static boolean isValid(Role value, Role[] acceptRoles) {
        if(value == null) {
            return false;
        }

        return Arrays.asList(acceptRoles).contains(value);
    }

}

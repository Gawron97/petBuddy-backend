package com.example.petbuddybackend.utils.annotations.validation;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AcceptRoleValidator implements ConstraintValidator<AcceptRole, Role> {

    private String[] acceptRoles;
    private String message;

    @Override
    public void initialize(AcceptRole constraintAnnotation) {
        acceptRoles = constraintAnnotation.acceptRole();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Role value, ConstraintValidatorContext context) {

        boolean isValid = isValid(value, acceptRoles);
        if(!isValid) {
            context.disableDefaultConstraintViolation();
            throw new UnauthorizedException(message);
        }
        return isValid;

    }

    private static boolean isValid(Role value, String[] acceptRoles) {
        if(value == null) {
            return true;
        }

        for(String acceptRole : acceptRoles) {
            if(value.name().equals(acceptRole)) {
                return true;
            }
        }

        return false;
    }

}

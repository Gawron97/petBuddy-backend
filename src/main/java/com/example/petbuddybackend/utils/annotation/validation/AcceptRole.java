package com.example.petbuddybackend.utils.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AcceptRoleValidator.class)
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AcceptRole {

    String message() default "Your profile is not allowed to do this action.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String[] acceptRole();

}

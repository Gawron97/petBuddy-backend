package com.example.petbuddybackend.utils.annotation.validation;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcceptRoleValidatorTest {

    private AcceptRoleValidator acceptRoleValidator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        acceptRoleValidator = new AcceptRoleValidator();
        acceptRoleValidator.initialize(new AcceptRole() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return AcceptRole.class;
            }

            @Override
            public String message() {
                return "Invalid role custom message";
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
            public String[] acceptRole() {
                return new String[]{"CARETAKER"};
            }
        });

    }

    @Test
    void testValidRole_WhenRoleIsValid_ShouldReturnTrue() {
        boolean result = acceptRoleValidator.isValid(Role.CARETAKER, context);
        assertTrue(result);
    }

    @Test
    void testValidRole_WhenRoleIsInValid_ShouldThrowUnauthorizeException() {
        assertThrows(UnauthorizedException.class, () -> acceptRoleValidator.isValid(Role.CLIENT, context));
    }

}

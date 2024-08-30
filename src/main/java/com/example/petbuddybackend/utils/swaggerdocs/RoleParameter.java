package com.example.petbuddybackend.utils.swaggerdocs;

import com.example.petbuddybackend.entity.user.Role;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Parameter(
        description = "Role of the principal user. Determines the user's selected profile.",
        schema = @Schema(implementation = Role.class)
)
public @interface RoleParameter {
}

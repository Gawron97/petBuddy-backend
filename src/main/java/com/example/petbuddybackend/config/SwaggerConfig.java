package com.example.petbuddybackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Pet Buddy API",
                description = "Pet Buddy API documentation"
        ),
        security = {
                @SecurityRequirement(name = "Bearer authentication")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "Bearer authentication",
                type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT",
                scheme = "bearer",
                in = SecuritySchemeIn.HEADER
        )
})
@Configuration
public class SwaggerConfig {
}


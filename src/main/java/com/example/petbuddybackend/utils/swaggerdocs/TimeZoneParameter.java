package com.example.petbuddybackend.utils.swaggerdocs;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Parameter(
        description = "The time zone to adjust the message timestamps to. If not provided, server's default timezone will be used.",
        schema = @Schema(type = "string", example = "Europe/Warsaw, CET, +00:02, ...")
)
public @interface TimeZoneParameter {
}

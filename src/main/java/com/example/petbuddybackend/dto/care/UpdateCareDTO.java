package com.example.petbuddybackend.dto.care;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record UpdateCareDTO(
        @NotNull LocalDate careStart,
        @NotNull LocalDate careEnd,
        @NotNull BigDecimal dailyPrice
        ) {
}

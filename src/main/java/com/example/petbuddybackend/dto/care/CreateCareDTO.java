package com.example.petbuddybackend.dto.care;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record CreateCareDTO(
        @NotNull LocalDate careStart,
        @NotNull LocalDate careEnd,
        String description,
        @NotNull BigDecimal dailyPrice,
        @NotBlank String animalType,
        List<Long> animalAttributeIds,
        @NotBlank String caretakerEmail,
        @NotBlank String clientEmail
        ) {
}

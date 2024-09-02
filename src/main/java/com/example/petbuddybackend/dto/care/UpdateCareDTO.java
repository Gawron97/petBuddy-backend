package com.example.petbuddybackend.dto.care;

import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@DateRange(startDateField = "careStart", endDateField = "careEnd", message = "End date of care must be after start date")
public record UpdateCareDTO(
        @NotNull
        @Future
        LocalDate careStart,

        @NotNull
        @Future
        LocalDate careEnd,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 5, fraction = 2)
        BigDecimal dailyPrice
        ) {
}

package com.example.petbuddybackend.dto.care;

import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import com.example.petbuddybackend.utils.annotation.validation.DateRangeField;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@DateRange(fields = {
        @DateRangeField(startDateField = "careStart", endDateField = "careEnd")
}, message = "End date of care must be after or same as start date")
public record CreateCareDTO(
        @NotNull
        @Future
        LocalDate careStart,

        @NotNull
        @Future
        LocalDate careEnd,

        String description,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 5, fraction = 2)
        BigDecimal dailyPrice,

        @NotBlank String animalType,
        List<Long> animalAttributeIds,
        @NotBlank String caretakerEmail,
        @NotBlank String clientEmail
        ) {
}

package com.example.petbuddybackend.dto.offer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record ModifyConfigurationDTO(
        String description,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 5, fraction = 2)
        BigDecimal dailyPrice,

        Map<String, @NotEmpty List<String>> selectedOptions
) {
}

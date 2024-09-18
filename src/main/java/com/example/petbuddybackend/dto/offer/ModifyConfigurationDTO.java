package com.example.petbuddybackend.dto.offer;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record ModifyConfigurationDTO(
        String description,
        BigDecimal dailyPrice,
        Map<String, List<String>> selectedOptions
) {
}

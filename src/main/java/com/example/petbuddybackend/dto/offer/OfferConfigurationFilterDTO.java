package com.example.petbuddybackend.dto.offer;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record OfferConfigurationFilterDTO(
        Map<String, List<String>> attributes,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
    public OfferConfigurationFilterDTO {
        minPrice = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        maxPrice = (maxPrice != null) ? maxPrice : BigDecimal.valueOf(Double.MAX_VALUE);
    }
}

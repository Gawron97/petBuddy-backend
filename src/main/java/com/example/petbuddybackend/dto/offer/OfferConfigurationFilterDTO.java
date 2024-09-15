package com.example.petbuddybackend.dto.offer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record OfferConfigurationFilterDTO(
        Map<String, List<String>> attributes,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}

package com.example.petbuddybackend.dto.offer;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record OfferConfigurationDTO(
        Long id,
        String description,
        BigDecimal dailyPrice,
        Map<String, List<String>> selectedOptions //it would be easier to get list of selected options id for
                                                  // persisting/editing, but map is better structure for presentation
) {
}

package com.example.petbuddybackend.dto.offer;

import java.util.List;

public record OfferFilterDTO(
        String animalType,
        List<OfferConfigurationFilterDTO> offerConfigurations
) {
}

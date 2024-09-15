package com.example.petbuddybackend.dto.offer;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record OfferFilterDTO(
        @NotBlank
        String animalType,
        List<OfferConfigurationFilterDTO> offerConfigurations
) {
}

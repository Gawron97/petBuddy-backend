package com.example.petbuddybackend.dto.offer;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record OfferFilterDTO(
        @NotBlank
        String animalType,
        List<OfferConfigurationFilterDTO> offerConfigurations,
        List<String> amenities
) {
}

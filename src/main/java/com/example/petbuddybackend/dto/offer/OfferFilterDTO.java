package com.example.petbuddybackend.dto.offer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
public record OfferFilterDTO(
        @NotBlank
        String animalType,
        List<@Valid OfferConfigurationFilterDTO> offerConfigurations
) {

    public OfferFilterDTO {
            if(offerConfigurations == null) {
                    offerConfigurations = Collections.emptyList();
            }
    }
}

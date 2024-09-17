package com.example.petbuddybackend.dto.offer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Builder
public record OfferFilterDTO(
        @NotBlank
        String animalType,
        Set<@Valid OfferConfigurationFilterDTO> offerConfigurations,
        Set<String> amenities
) {

        public OfferFilterDTO {
                if(offerConfigurations == null) {
                        offerConfigurations = Collections.emptySet();
                }
                if(amenities == null) {
                        amenities = Collections.emptySet();
                }
        }
}

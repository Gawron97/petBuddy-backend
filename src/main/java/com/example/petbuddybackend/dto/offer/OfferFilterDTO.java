package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.availability.AvailabilityFilterDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Collections;
import java.util.Set;

@Builder
public record OfferFilterDTO(
        @NotBlank
        String animalType,
        Set<@Valid OfferConfigurationFilterDTO> offerConfigurations,
        Set<String> amenities,
        Set<@Valid AvailabilityFilterDTO> availabilities
) {

        public OfferFilterDTO {
                if(offerConfigurations == null) {
                        offerConfigurations = Collections.emptySet();
                }
                if(amenities == null) {
                        amenities = Collections.emptySet();
                }
                if(availabilities == null) {
                        availabilities = Collections.emptySet();
                }
        }
}

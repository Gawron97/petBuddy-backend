package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.amenity.AmenityDTO;
import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.dto.availability.AvailabilityRangeDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record OfferDTO(
        Long id,
        String description,
        AnimalDTO animal,
        List<OfferConfigurationDTO> offerConfigurations,
        List<String> animalAmenities,
        List<AvailabilityRangeDTO> availabilities
) {
}

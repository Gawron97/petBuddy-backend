package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.amenity.AmenityDTO;
import com.example.petbuddybackend.dto.animal.AnimalDTO;

import java.util.List;

public record OfferDTO(
        Long id,
        String description,
        AnimalDTO animal,
        List<OfferConfigurationDTO> offerConfigurations,
        List<String> animalAmenities
) {
}

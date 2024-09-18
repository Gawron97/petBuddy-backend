package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.animal.AnimalDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record ModifyOfferDTO(
        String description,
        AnimalDTO animal,
        List<ModifyConfigurationDTO> offerConfigurations,
        List<String> animalAmenities
) {
}

package com.example.petbuddybackend.dto.animal;

import com.example.petbuddybackend.entity.animal.AnimalType;
import lombok.Builder;

import java.util.Optional;

@Builder
public record AnimalPreferenceDTO(
        Long id,
        Optional<AnimalType> animalType,
        Optional<String> animalDetails
) {
}

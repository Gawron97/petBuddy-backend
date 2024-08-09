package com.example.petbuddybackend.dto.animal;

import com.example.petbuddybackend.entity.animal.AnimalType;

public record CaretakerOfferDTO(
        Long id,
        AnimalType animalType,
        String animalDetails,
        String dailyPrice) {
}

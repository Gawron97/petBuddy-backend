package com.example.petbuddybackend.dto.offer;

import com.example.petbuddybackend.dto.animal.AnimalPreferenceDTO;
import lombok.Builder;

import java.util.Optional;

@Builder
public record CaretakerOfferDTO(
        Optional<Long> id,
        AnimalPreferenceDTO animalPreference,
        Double dailyPrice) {
}

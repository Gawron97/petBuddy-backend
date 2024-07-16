package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.AnimalType;
import lombok.Builder;

import java.util.Set;

@Builder
public record CaretakerSearchCriteria(
        String personalDataLike,
        String cityLike,
        Voivodeship voivodeship,
        Set<AnimalType> animalTypes
) {
}

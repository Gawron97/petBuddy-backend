package com.example.petbuddybackend.dto.animal;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record AnimalWithAttributesDTO(
        String animalType,
        Map<String, List<String>> animalAttributes
) {
}

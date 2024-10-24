package com.example.petbuddybackend.dto.animal;

import java.util.List;
import java.util.Map;

public record AnimalComplexInfoDTO(
        String animalType,
        Map<String, List<String>> animalAttributes,
        List<String> amenities
) {
}

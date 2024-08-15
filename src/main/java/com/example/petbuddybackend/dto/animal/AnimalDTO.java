package com.example.petbuddybackend.dto.animal;

import lombok.Builder;

@Builder
public record AnimalDTO(
        String animalType
) {
}

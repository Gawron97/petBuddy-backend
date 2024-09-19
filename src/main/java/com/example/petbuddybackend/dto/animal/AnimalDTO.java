package com.example.petbuddybackend.dto.animal;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AnimalDTO(
        @NotBlank
        String animalType
) {
}

package com.example.petbuddybackend.dto.animal;

import com.example.petbuddybackend.entity.animal.AnimalType;

public record AnimalDTO(
        Long id,
        AnimalType animalType) {
}
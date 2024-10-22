package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.service.animal.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/animal")
public class AnimalController {

    private final AnimalService animalService;

    @SecurityRequirements
    @Operation(summary = "Get animal attributes for specified animal type")
    @GetMapping("attributes/{animalType}")
    public Map<String, List<String>> getAnimalAttributes(@PathVariable String animalType) {
        return animalService.getAnimalAttributesOfAnimal(animalType);
    }

    @SecurityRequirements
    @Operation(summary = "Get all animals")
    @GetMapping
    public Set<String> getAnimals() {
        return animalService.getAnimals();
    }

    @SecurityRequirements
    @Operation(summary = "Get amenities available for specified animal type")
    @GetMapping("amenities/{animalType}")
    public Set<String> getAmenities(@PathVariable String animalType) {
        return animalService.getAmenitiesForAnimal(animalType);
    }

}

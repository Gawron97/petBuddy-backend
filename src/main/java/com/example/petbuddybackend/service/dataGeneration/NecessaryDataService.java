package com.example.petbuddybackend.service.dataGeneration;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.utils.provider.resources.AnimalAttributeProvider;
import com.example.petbuddybackend.utils.provider.resources.AnimalProvider;
import com.example.petbuddybackend.utils.resourceStructure.AnimalAttributeConfig;
import com.example.petbuddybackend.utils.resourceStructure.AnimalConfig;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("dev | test")
@RequiredArgsConstructor
public class NecessaryDataService {

    private final Faker faker = new Faker();

    private final AnimalProvider animalProvider;
    private final AnimalAttributeProvider animalAttributeProvider;

    public List<Animal> createAnimals() {
        return animalProvider.getAnimalConfigs().stream()
                .map(this::createAnimal)
                .toList();
    }

    private Animal createAnimal(AnimalConfig animalConfig) {
        return Animal.builder()
                .animalType(animalConfig.animalType())
                .build();
    }

    private Animal createAnimal(String animalType) {
        return Animal.builder()
                .animalType(animalType)
                .build();
    }

    private Animal getAnimal(String animalType, List<Animal> animals) {
        return animals.stream()
                .filter(animal -> animal.getAnimalType().equals(animalType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Animal not found during generation of animal attributes"));
    }

    public List<AnimalAttribute> createAnimalAttributes(List<Animal> animals) {
        return animalAttributeProvider.getAnimalAttributeConfigs().stream()
                .map(animalAttributeConfig ->
                        createAnimalAttribute(animalAttributeConfig, getAnimal(animalAttributeConfig.animalType(), animals)))
                .toList();
    }

    private AnimalAttribute createAnimalAttribute(AnimalAttributeConfig animalAttributeConfig, Animal animal) {
        return AnimalAttribute.builder()
                .animal(animal)
                .attributeName(animalAttributeConfig.attributeName())
                .attributeValue(animalAttributeConfig.attributeValue())
                .build();
    }

}

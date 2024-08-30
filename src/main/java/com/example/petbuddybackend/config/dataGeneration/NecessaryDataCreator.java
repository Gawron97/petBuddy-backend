package com.example.petbuddybackend.config.dataGeneration;

import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.repository.amenity.AmenityRepository;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.service.datageneration.NecessaryDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Slf4j
@Profile("dev | test")
@RequiredArgsConstructor
public class NecessaryDataCreator {

    private final NecessaryDataService necessaryDataService;

    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;
    private final AmenityRepository amenityRepository;
    private final AnimalAmenityRepository animalAmenityRepository;

    private List<Animal> animals;
    private List<Amenity> amenities;

    public void createData() {

        if (shouldSkipInit()) {
            return;
        }
        log.info("Creating necessary data in database...");

        if (animalRepository.count() == 0) {
            animals = animalRepository.saveAllAndFlush(necessaryDataService.createAnimals());
        }

        if (animalAttributeRepository.count() == 0) {
            animalAttributeRepository.saveAllAndFlush(necessaryDataService.createAnimalAttributes(animals));
        }

        if (amenityRepository.count() == 0) {
            amenities = amenityRepository.saveAllAndFlush(necessaryDataService.createAmenities());
        }

        if (animalAmenityRepository.count() == 0) {
            animalAmenityRepository.saveAllAndFlush(necessaryDataService.createAnimalAmenities(amenities, animals));
        }


        log.info("Necessary data created successfully!");

        cleanCache();

    }

    private boolean shouldSkipInit() {
        return animalAttributeRepository.count() != 0 &&
                animalRepository.count() != 0 &&
                amenityRepository.count() != 0 &&
                animalAmenityRepository.count() != 0;


    }

    private void cleanCache() {
        animals.clear();
        amenities.clear();
    }

}

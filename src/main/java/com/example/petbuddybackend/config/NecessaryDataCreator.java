package com.example.petbuddybackend.config;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.service.dataGeneration.NecessaryDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class NecessaryDataCreator {

    private final NecessaryDataService necessaryDataService;

    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;

    private List<Animal> animals;

    public void createData() {

        if(shouldSkipInit()){
            return;
        }
        log.info("Creating necessary data in database...");

        if(animalRepository.count() == 0) {
           animals = animalRepository.saveAllAndFlush(necessaryDataService.createAnimals());
        }

        if(animalAttributeRepository.count() == 0) {
            animalAttributeRepository.saveAllAndFlush(necessaryDataService.createAnimalAttributes(animals));
        }


        log.info("Necessary data created successfully!");

    }

    private boolean shouldSkipInit() {
        return animalAttributeRepository.count() != 0 &&
                animalRepository.count() != 0;


    }

}

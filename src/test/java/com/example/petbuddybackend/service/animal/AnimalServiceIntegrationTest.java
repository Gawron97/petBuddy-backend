package com.example.petbuddybackend.service.animal;

import com.example.petbuddybackend.dto.animal.AnimalComplexInfoDTO;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class AnimalServiceIntegrationTest {

    @Autowired
    private AnimalService animalService;

    @Test
    void getAnimalAmenity() {
        assertNotNull(animalService.getAnimalAmenity("toys", "DOG"));
    }

    @Test
    void getAnimalAmenity_WhenAmenityDoesNotExists_ThenThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> animalService.getAnimalAmenity("sth", "CAT"));
    }

    @Test
    void getAnimalAttribute() {
        assertNotNull(animalService.getAnimalAttribute("DOG", "SIZE", "BIG"));
    }

    @Test
    void getAnimalAttribute_WhenAttributeDoesNotExists_ThenThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> animalService.getAnimalAttribute("DOG", "sth", "sth"));
    }

    @Test
    void getAnimal() {
        assertNotNull(animalService.getAnimal("DOG"));
    }

    @Test
    void getAnimal_WhenAnimalDoesNotExists_ThenThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> animalService.getAnimal("NOAAAAA"));
    }

    @Test
    void getAnimalAttributes() {
        Set<AnimalAttribute> animalAttributes = animalService.getAnimalAttributesOfAnimal(List.of(1L, 2L));
        assertNotNull(animalAttributes);
        assertEquals(2, animalAttributes.size());
    }

    @Test
    void getAnimalAttributes_WhenDuplicated_ThenFlatten() {
        Set<AnimalAttribute> animalAttributes = animalService.getAnimalAttributesOfAnimal(List.of(1L, 1L));
        assertNotNull(animalAttributes);
        assertEquals(1, animalAttributes.size());
    }

    @Test
    void getAnimalAttributesOfAnimal() {
        Map<String, List<String>> result = animalService.getAnimalAttributesOfAnimal("DOG");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAnimals() {
        Set<String> animals = animalService.getAnimals();
        assertNotNull(animals);
        assertFalse(animals.isEmpty());
    }

    @Test
    void getAmenitiesForAnimal() {
        Set<String> amenities = animalService.getAmenitiesForAnimal("DOG");
        assertNotNull(amenities);
        assertFalse(amenities.isEmpty());
    }

    @Test
    @Transactional
    void getAnimalsWithAttributesAndAmenities() {
        List<AnimalComplexInfoDTO> animalsWithAttributesAndAmenities = animalService.getAnimalsWithAttributesAndAmenities();
        assertNotNull(animalsWithAttributesAndAmenities);
        assertFalse(animalsWithAttributesAndAmenities.isEmpty());
    }

}

package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.animal.AnimalComplexInfoDTO;
import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockAnimalProvider;
import org.junit.jupiter.api.Test;

import static com.example.petbuddybackend.testutils.mock.MockAnimalProvider.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnimalMapperTest {

    private final AnimalMapper animalMapper = AnimalMapper.INSTANCE;

    @Test
    void mapToAnimalDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        Animal animal = createMockAnimal("DOG");
        AnimalDTO animalDTO = animalMapper.mapToAnimalDTO(animal);
        assertNotNull(animalDTO);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(animalDTO));
    }

    @Test
    void mapToAnimalComplexInfoDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        Animal animal = MockAnimalProvider.createMockAnimal("DOG");
        animal.getAnimalAttributes().add(createAnimalAttribute("attributeName", "attributeValue", animal));
        animal.getAnimalAmenities().add(createAnimalAmenity(animal, "amenity"));

        AnimalComplexInfoDTO animalDTO = animalMapper.mapToAnimalComplexInfoDTO(animal);
        assertNotNull(animalDTO);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(animalDTO));
    }

}

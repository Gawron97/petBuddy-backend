package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareDTO;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.example.petbuddybackend.testutils.ValidationUtils.fieldsNotNullRecursive;
import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CareMapperTest {

    private final CareMapper mapper = CareMapper.INSTANCE;

    @Test
    void mapToCareDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        Caretaker caretaker = createMockCaretaker();
        Client client = createMockClient();
        Animal animal = Animal.builder().animalType("DOG").build();
        Care care = createMockCare(caretaker, client, animal);
        Set<AnimalAttribute> animalAttributes = new HashSet<>();
        animalAttributes.add(
                AnimalAttribute.builder()
                        .animal(animal)
                        .attributeName("attributeName")
                        .attributeValue("attributeValue")
                        .build()
        );
        care.setAnimalAttributes(animalAttributes);
        care.setId(1L);
        care.setSubmittedAt(ZonedDateTime.now());

        CareDTO careDTO = mapper.mapToCareDTO(care, ZoneId.systemDefault());

        assertTrue(fieldsNotNullRecursive(careDTO));
    }

    @Test
    void mapToDetailedCareDTO_shouldNotLeaveNullFields() {
        Caretaker caretaker = createMockCaretakerWithPhoto("caretakerEmail");
        Client client = createMockClientWithPhoto("clientEmail");
        Animal animal = Animal.builder().animalType("DOG").build();
        Care care = createMockCare(caretaker, client, animal);
        Set<AnimalAttribute> animalAttributes = new HashSet<>();
        animalAttributes.add(
                AnimalAttribute.builder()
                        .animal(animal)
                        .attributeName("attributeName")
                        .attributeValue("attributeValue")
                        .build()
        );
        care.setAnimalAttributes(animalAttributes);
        care.setId(1L);
        care.setSubmittedAt(ZonedDateTime.now());

        DetailedCareDTO careDTO = mapper.mapToDetailedCareDTO(care, ZoneId.systemDefault(), true);

        assertTrue(fieldsNotNullRecursive(careDTO));
    }

}

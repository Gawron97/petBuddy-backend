package com.example.petbuddybackend.service.offer;


import com.example.petbuddybackend.config.TestDataConfiguration;
import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class OfferServiceIntegrationTest {

    @Autowired
    private OfferService offerService;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AnimalAttributeRepository animalAttributeRepository;

    @Autowired
    private AnimalAmenityRepository animalAmenityRepository;

    private Caretaker caretakerWithComplexOffer;
    private Animal animalInComplexOffer;
    private List<AnimalAttribute> animalAttributesInComplexOffer;
    private List<AnimalAmenity> animalAmenitiesInComplexOffer;
    private Offer existingOffer;

    @BeforeEach
    void setUp() {
        caretakerWithComplexOffer = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        animalInComplexOffer = animalRepository.findById("DOG").orElseThrow();
        animalAttributesInComplexOffer = Arrays.asList(
                animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                        "DOG", "SIZE", "BIG").orElseThrow(),
                animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                        "DOG", "SEX", "MALE").orElseThrow()
        );
        animalAmenitiesInComplexOffer = Arrays.asList(
                animalAmenityRepository.findByAmenity_AmenityAndAnimal_AnimalType("toys", "DOG").orElseThrow()
        );

    }

    @Transactional
    @ParameterizedTest
    @MethodSource("provideOfferConfigurations")
    void givenExistingOfferWithConfiguration_whenAddOfferWithAnotherConfiguration_ThenAddConfigurationToExistingOffer(
            OfferDTO offerToSave, boolean expectedToBeExistingOffer, int expectedNumberOfConfigurationsAfterAddition,
            int expectedNumberOfAnimalAmenities) {

        if(expectedToBeExistingOffer) {
            existingOffer = PersistenceUtils.addComplexOffer(caretakerWithComplexOffer, animalInComplexOffer,
                    animalAttributesInComplexOffer, animalAmenitiesInComplexOffer, offerRepository);
        }

        // When
        OfferDTO resultOfferDTO = offerService.addOrEditOffer(offerToSave, caretakerWithComplexOffer.getEmail());

        // Then
        assertNotNull(resultOfferDTO);
        assertEquals(expectedNumberOfConfigurationsAfterAddition, resultOfferDTO.offerConfigurations().size());
        assertEquals(expectedNumberOfAnimalAmenities, resultOfferDTO.animalAmenities().size());

    }

    static Stream<Arguments> provideOfferConfigurations() {
        return Stream.of(
                Arguments.of(
                        OfferDTO.builder()
                                .description("First Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                OfferConfigurationDTO.builder()
                                                        .description("First Description")
                                                        .dailyPrice(20.0)
                                                        .selectedOptions(new HashMap<>(Map.of("SIZE", new ArrayList<>(List.of("BIG")))))
                                                        .build()
                                        ))
                                )
                                .build(),
                        false, // Expected to be a new offer
                        1, // Expected number of configurations after addition
                        0 // Expected number of animal amenities
                ),
                Arguments.of(
                        OfferDTO.builder()
                                .description("Second Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                OfferConfigurationDTO.builder()
                                                        .description("Second Description")
                                                        .dailyPrice(30.0)
                                                        .selectedOptions(new HashMap<>(Map.of("SIZE", new ArrayList<>(List.of("SMALL")))))
                                                        .build()
                                        ))
                                )
                                .build(),
                        true , // Expected to be an existing offer
                        2, // Expected number of configurations after addition
                        1 // Expected number of animal amenities
                )
        );
    }


}

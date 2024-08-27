package com.example.petbuddybackend.service.offer;


import com.example.petbuddybackend.config.TestDataConfiguration;
import com.example.petbuddybackend.dto.animal.AnimalDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.offer.OfferConfigurationDuplicatedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
    private OfferConfigurationRepository offerConfigurationRepository;

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
                animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
        );

        existingOffer = PersistenceUtils.addComplexOffer(caretakerWithComplexOffer, animalInComplexOffer,
                animalAttributesInComplexOffer, BigDecimal.valueOf(10.0), animalAmenitiesInComplexOffer, offerRepository);

    }

    @AfterEach
    void tearDown() {
        if(existingOffer != null) {
            offerRepository.delete(existingOffer);
        }
        caretakerRepository.delete(caretakerWithComplexOffer);
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("provideOfferConfigurations")
    void addOrEditOffer_ShouldAddOrEditOfferWithProperData(
            OfferDTO offerToSave, boolean expectedToBeExistingOffer, int expectedNumberOfConfigurationsAfterAddition,
            int expectedNumberOfAnimalAmenities) {

        if(!expectedToBeExistingOffer) {
            offerRepository.delete(existingOffer);
            existingOffer = null;
        }

        // When
        OfferDTO resultOfferDTO = offerService.addOrEditOffer(offerToSave, caretakerWithComplexOffer.getEmail());

        // Then
        assertNotNull(resultOfferDTO);

        Offer offer = offerRepository.findById(resultOfferDTO.id()).orElseThrow();
        if(expectedNumberOfConfigurationsAfterAddition > 0) {
            assertEquals(expectedNumberOfConfigurationsAfterAddition, offer.getOfferConfigurations().size());
        } else {
            assertNull(offer.getOfferConfigurations());
        }
        if(expectedNumberOfAnimalAmenities > 0) {
            assertEquals(expectedNumberOfAnimalAmenities, offer.getAnimalAmenities().size());
        } else {
            assertNull(offer.getAnimalAmenities());
        }

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
                                                        .dailyPrice(BigDecimal.valueOf(20.0))
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
                                                        .dailyPrice(BigDecimal.valueOf(30.0))
                                                        .selectedOptions(new HashMap<>(Map.of("SIZE", new ArrayList<>(List.of("SMALL")))))
                                                        .build()
                                        ))
                                )
                                .build(),
                        true , // Expected to be an existing offer
                        2, // Expected number of configurations after addition
                        1 // Expected number of animal amenities
                ),
                Arguments.of(
                        OfferDTO.builder()
                                .description("Third Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                OfferConfigurationDTO.builder()
                                                        .description("Second Description")
                                                        .dailyPrice(BigDecimal.valueOf(30.0))
                                                        .selectedOptions(new HashMap<>(Map.of("SIZE", new ArrayList<>(List.of("SMALL")))))
                                                        .build()
                                        ))
                                )
                                .animalAmenities(new ArrayList<>(List.of("garden")))
                                .build(),
                        true , // Expected to be an existing offer
                        2, // Expected number of configurations after addition
                        2 // Expected number of animal amenities
                ),
                Arguments.of(
                        OfferDTO.builder()
                                .description("Third Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .animalAmenities(new ArrayList<>(List.of("garden")))
                                .build(),
                        true , // Expected to be an existing offer
                        1, // Expected number of configurations after addition
                        2 // Expected number of animal amenities
                )
        );
    }


    @ParameterizedTest
    @MethodSource("provideEditConfigurationScenarios")
    @Transactional
    void editConfiguration_ShouldEditConfigurationWithProperData(
            int configurationIndex, OfferConfigurationDTO configurationToEdit, String expectedDescription,
            BigDecimal expectedDailyPrice, Map<String, List<String>> expectedSelectedOptions) {

        Long configurationId = existingOffer.getOfferConfigurations().get(configurationIndex).getId();
        PersistenceUtils.addOfferConfigurationForOffer(existingOffer,
                List.of(animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                        "DOG", "SIZE", "SMALL").orElseThrow()),
                offerRepository);

        // When
        OfferConfigurationDTO resultConfigDTO = offerService.editConfiguration(configurationId, configurationToEdit);

        // Then
        assertNotNull(resultConfigDTO);

        OfferConfiguration offerConfiguration = offerConfigurationRepository.findById(resultConfigDTO.id()).orElseThrow();
        assertEquals(expectedDescription, offerConfiguration.getDescription());
        assertEquals(expectedDailyPrice, offerConfiguration.getDailyPrice());

        Map<String, List<String>> selectedOptions = offerConfiguration.getOfferOptions().stream()
                .collect(Collectors.groupingBy(
                        offerOption -> offerOption.getAnimalAttribute().getAttributeName(),
                        Collectors.mapping(
                                offerOption -> offerOption.getAnimalAttribute().getAttributeValue(),
                                Collectors.toList()
                        )
                ));

        assertEquals(expectedSelectedOptions, selectedOptions);
    }

    static Stream<Arguments> provideEditConfigurationScenarios() {
        return Stream.of(
                Arguments.of(
                        0, // ID of the configuration to edit
                        OfferConfigurationDTO.builder()
                                .description("Updated Description")
                                .dailyPrice(BigDecimal.valueOf(25.0))
                                .selectedOptions(new HashMap<>(Map.of(
                                        "SIZE", List.of("BIG"),
                                        "SEX", List.of("MALE")
                                )))
                                .build(),
                        "Updated Description", // Expected Description
                        BigDecimal.valueOf(25.0), // Expected Daily Price
                        Map.of("SIZE", List.of("BIG"), "SEX", List.of("MALE")) // Expected Selected Options
                ),
                Arguments.of(
                        0, // ID of the configuration to edit
                        OfferConfigurationDTO.builder()
                                .description("Another Update")
                                .dailyPrice(BigDecimal.valueOf(30.0))
                                .selectedOptions(new HashMap<>(Map.of(
                                        "SIZE", List.of("SMALL"),
                                        "SEX", List.of("MALE", "SHE")
                                )))
                                .build(),
                        "Another Update", // Expected Description
                        BigDecimal.valueOf(30.0), // Expected Daily Price
                        Map.of(
                                "SIZE", List.of("SMALL"),
                                "SEX", List.of("MALE", "SHE")
                        ) // Expected Selected Options
                )
        );
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("provideIncorrectOfferConfigurations")
    void addOrEditOffer_ShouldThrowException(
            OfferDTO offerToSave, boolean expectedToBeExistingOffer, Class expectedExceptionClass) {

        if(!expectedToBeExistingOffer) {
            offerRepository.delete(existingOffer);
            existingOffer = null;
        }

        assertThrows(expectedExceptionClass,
                () -> offerService.addOrEditOffer(offerToSave, caretakerWithComplexOffer.getEmail()));

    }

    static Stream<Arguments> provideIncorrectOfferConfigurations() {
        return Stream.of(
                Arguments.of(
                        OfferDTO.builder()
                                .description("First Configuration")
                                .animal(AnimalDTO.builder().animalType("SOME ANIMAL").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                OfferConfigurationDTO.builder()
                                                        .description("First Description")
                                                        .dailyPrice(BigDecimal.valueOf(20.0))
                                                        .selectedOptions(new HashMap<>(Map.of("SIZE", new ArrayList<>(List.of("BIG")))))
                                                        .build()
                                        ))
                                )
                                .build(),
                        false, // Expected to be a new offer
                        NotFoundException.class
                ),
                Arguments.of(
                        OfferDTO.builder()
                                .description("Second Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                OfferConfigurationDTO.builder()
                                                        .description("Second Description")
                                                        .dailyPrice(BigDecimal.valueOf(30.0))
                                                        .selectedOptions(new HashMap<>(
                                                                Map.of(
                                                                        "SIZE", new ArrayList<>(List.of("BIG")),
                                                                        "SEX", new ArrayList<>(List.of("MALE"))
                                                        )))
                                                        .build()
                                        ))
                                )
                                .build(),
                        true , // Expected to be an existing offer
                        OfferConfigurationDuplicatedException.class
                ),
                Arguments.of(
                        OfferDTO.builder()
                                .description("Third Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .animalAmenities(new ArrayList<>(List.of("not found amenity")))
                                .build(),
                        true , // Expected to be an existing offer
                        NotFoundException.class
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEditConfigurationScenarios")
    @Transactional
    void editConfiguration_ShouldThrowAnException(
            int configurationIndex, OfferConfigurationDTO configurationToEdit, Class expectedExceptionClass) {

        Long configurationId = existingOffer.getOfferConfigurations().get(configurationIndex).getId();

        PersistenceUtils.addOfferConfigurationForOffer(existingOffer,
                List.of(animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                        "DOG", "SIZE", "SMALL").orElseThrow()),
                offerRepository);

        assertThrows(expectedExceptionClass,
                () -> offerService.editConfiguration(configurationId, configurationToEdit));
    }

    static Stream<Arguments> provideIncorrectEditConfigurationScenarios() {
        return Stream.of(
                Arguments.of(
                        0, // ID of the configuration to edit
                        OfferConfigurationDTO.builder()
                                .description("Updated Description")
                                .dailyPrice(BigDecimal.valueOf(25.0))
                                .selectedOptions(new HashMap<>(Map.of(
                                        "SIZE", List.of("SMALL")
                                )))
                                .build(),
                        OfferConfigurationDuplicatedException.class
                )
        );
    }

    @Test
    @Transactional
    void deleteConfiguration_ShouldDeleteConfiguration() {
        // Given
        Long configurationId = existingOffer.getOfferConfigurations().get(0).getId();
        int expectedNumberOfConfigurationsAfterDeletion = existingOffer.getOfferConfigurations().size() - 1;

        // When
        offerService.deleteConfiguration(configurationId);

        // Then
        Offer offerAfterDeletion = offerRepository.findById(existingOffer.getId()).orElseThrow();
        assertEquals(expectedNumberOfConfigurationsAfterDeletion, offerAfterDeletion.getOfferConfigurations().size());
    }




}

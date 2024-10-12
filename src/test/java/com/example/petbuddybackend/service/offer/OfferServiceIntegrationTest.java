package com.example.petbuddybackend.service.offer;


import com.example.petbuddybackend.dto.availability.AvailabilityRangeDTO;
import com.example.petbuddybackend.dto.availability.CreateOffersAvailabilityDTO;
import com.example.petbuddybackend.dto.offer.ModifyConfigurationDTO;
import com.example.petbuddybackend.dto.offer.ModifyOfferDTO;
import com.example.petbuddybackend.repository.availability.AvailabilityRepository;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
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
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import com.example.petbuddybackend.utils.exception.throweable.offer.AnimalAmenityDuplicatedInOfferException;
import com.example.petbuddybackend.utils.exception.throweable.offer.AvailabilityDatesOverlappingException;
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
import java.time.ZonedDateTime;
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

    @Autowired
    private AvailabilityRepository availabilityRepository;

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
                animalAttributesInComplexOffer, BigDecimal.valueOf(10.0), animalAmenitiesInComplexOffer, Set.of(), offerRepository);

    }

    @AfterEach
    @Transactional
    void tearDown() {
        caretakerRepository.delete(caretakerWithComplexOffer);
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("provideOfferConfigurations")
    void addOrEditOffer_ShouldAddOrEditOfferWithProperData(
            ModifyOfferDTO offerToSave, boolean expectedToBeExistingOffer, int expectedNumberOfConfigurationsAfterAddition,
            int expectedNumberOfAnimalAmenities) {

        if(!expectedToBeExistingOffer) {
            offerRepository.delete(existingOffer);
            caretakerWithComplexOffer.getOffers().remove(existingOffer);
            caretakerRepository.save(caretakerWithComplexOffer);
            existingOffer = null;
        }

        // When
        OfferDTO resultOfferDTO = offerService.addOrEditOffer(offerToSave, caretakerWithComplexOffer.getEmail());

        // Then
        assertNotNull(resultOfferDTO);

        Offer offer = offerRepository.findById(resultOfferDTO.id()).orElseThrow();

        assertEquals(expectedNumberOfConfigurationsAfterAddition, offer.getOfferConfigurations().size());
        assertEquals(expectedNumberOfAnimalAmenities, offer.getAnimalAmenities().size());

    }

    static Stream<Arguments> provideOfferConfigurations() {
        return Stream.of(
                Arguments.of(
                        ModifyOfferDTO.builder()
                                .description("First Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                ModifyConfigurationDTO.builder()
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
                        ModifyOfferDTO.builder()
                                .description("Second Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                ModifyConfigurationDTO.builder()
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
                        ModifyOfferDTO.builder()
                                .description("Third Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                ModifyConfigurationDTO.builder()
                                                        .description("Second Description")
                                                        .dailyPrice(BigDecimal.valueOf(30.0))
                                                        .selectedOptions(new HashMap<>(Map.of("SIZE", new ArrayList<>(List.of("SMALL")))))
                                                        .build()
                                        ))
                                )
                                .animalAmenities(new HashSet<>(List.of("garden")))
                                .build(),
                        true , // Expected to be an existing offer
                        2, // Expected number of configurations after addition
                        2 // Expected number of animal amenities
                ),
                Arguments.of(
                        ModifyOfferDTO.builder()
                                .description("Third Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .animalAmenities(new HashSet<>(List.of("garden")))
                                .build(),
                        true , // Expected to be an existing offer
                        1, // Expected number of configurations after addition
                        2 // Expected number of animal amenities
                )
        );
    }

    @Test
    @Transactional
    void addConfigurationsForOffer_shouldAddConfigurationsProperly() {

        //Given
        List<ModifyConfigurationDTO> configurationsToAdd = List.of(
                ModifyConfigurationDTO.builder()
                        .description("Second Description")
                        .dailyPrice(BigDecimal.valueOf(20.0))
                        .selectedOptions(new HashMap<>(
                                Map.of(
                                        "SIZE", new ArrayList<>(List.of("SMALL"))
                                )
                        ))
                        .build(),
                ModifyConfigurationDTO.builder()
                        .description("Third Description")
                        .dailyPrice(BigDecimal.valueOf(30.0))
                        .selectedOptions(new HashMap<>(
                                Map.of(
                                        "SIZE", new ArrayList<>(List.of("SMALL")),
                                        "SEX", new ArrayList<>(List.of("SHE"))
                                )
                        ))
                        .build()
        );

        //When
        OfferDTO resultOfferDTO = offerService.addConfigurationsForOffer(
                existingOffer.getId(),
                configurationsToAdd,
                caretakerWithComplexOffer.getEmail()
        );

        //Then
        assertNotNull(resultOfferDTO);
        Offer offerAfterAddition = offerRepository.findById(resultOfferDTO.id()).orElseThrow();
        assertEquals(3, offerAfterAddition.getOfferConfigurations().size());

    }

    @Test
    @Transactional
    void addConfigurationsForOffer_whenAddingByNotOwner_ShouldThrowUnauthorizedException() {

        //Given
        List<ModifyConfigurationDTO> configurationsToAdd = List.of();

        //When Then
        assertThrows(UnauthorizedException.class,
                () -> offerService.addConfigurationsForOffer(
                        existingOffer.getId(),
                        configurationsToAdd,
                        "badEmail"
                ));

    }

    @Test
    @Transactional
    void addConfigurationsForOffer_whenAddingDuplicateConfiguration_shouldThrowOfferConfigurationDuplicatedException() {

        //Given
        List<ModifyConfigurationDTO> configurationsToAdd = List.of(
                ModifyConfigurationDTO.builder()
                        .description("Second Description")
                        .dailyPrice(BigDecimal.valueOf(20.0))
                        .selectedOptions(new HashMap<>(
                                Map.of(
                                        "SIZE", new ArrayList<>(List.of("SMALL"))
                                )
                        ))
                        .build(),
                ModifyConfigurationDTO.builder()
                        .description("Third Description")
                        .dailyPrice(BigDecimal.valueOf(30.0))
                        .selectedOptions(new HashMap<>(
                                Map.of(
                                        "SIZE", new ArrayList<>(List.of("BIG")),
                                        "SEX", new ArrayList<>(List.of("MALE"))
                                )
                        ))
                        .build()
        );

        //When Then
        assertThrows(OfferConfigurationDuplicatedException.class,
                () -> offerService.addConfigurationsForOffer(
                        existingOffer.getId(),
                        configurationsToAdd,
                        caretakerWithComplexOffer.getEmail()
                ));

    }

    @Test
    @Transactional
    void addAmenitiesForOffer_ShouldAddAmenitiesProperly() {

        //Given
        Set<String> amenitiesToAdd = Set.of("garden");

        //When
        OfferDTO resultOfferDTO = offerService.addAmenitiesForOffer(
                existingOffer.getId(),
                amenitiesToAdd,
                caretakerWithComplexOffer.getEmail()
        );

        //Then
        assertNotNull(resultOfferDTO);
        Offer offerAfterAddition = offerRepository.findById(resultOfferDTO.id()).orElseThrow();
        assertEquals(2, offerAfterAddition.getAnimalAmenities().size());

    }

    @Test
    @Transactional
    void addAmenitiesForOffer_whenAddingByNotOwner_ShouldThrowUnauthorizedException() {

        //Given
        Set<String> amenitiesToAdd = Set.of("garden");

        //When Then
        assertThrows(UnauthorizedException.class,
                () -> offerService.addAmenitiesForOffer(
                        existingOffer.getId(),
                        amenitiesToAdd,
                        "badEmail"
                ));

    }

    @Test
    @Transactional
    void addAmenitiesForOffer_whenAddingDuplicateAmenity_ShouldThrowAnimalAmenityDuplicatedInOfferException() {

        //Given
        Set<String> amenitiesToAdd = Set.of("toys", "garden");

        //When Then
        assertThrows(AnimalAmenityDuplicatedInOfferException.class,
                () -> offerService.addAmenitiesForOffer(
                        existingOffer.getId(),
                        amenitiesToAdd,
                        caretakerWithComplexOffer.getEmail()
                ));

    }

    @ParameterizedTest
    @MethodSource("provideEditConfigurationScenarios")
    @Transactional
    void editConfiguration_ShouldEditConfigurationWithProperData(
            int configurationIndex, ModifyConfigurationDTO configurationToEdit, String expectedDescription,
            BigDecimal expectedDailyPrice, Map<String, List<String>> expectedSelectedOptions) {

        Long configurationId = existingOffer.getOfferConfigurations().get(configurationIndex).getId();
        PersistenceUtils.addOfferConfigurationForOffer(existingOffer,
                List.of(animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                        "DOG", "SIZE", "SMALL").orElseThrow()),
                offerRepository);

        // When
        OfferConfigurationDTO resultConfigDTO = offerService.editConfiguration(configurationId, configurationToEdit, caretakerWithComplexOffer.getEmail());

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
                        ModifyConfigurationDTO.builder()
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
                        ModifyConfigurationDTO.builder()
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
            ModifyOfferDTO offerToSave, boolean expectedToBeExistingOffer, Class expectedExceptionClass) {

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
                        ModifyOfferDTO.builder()
                                .description("First Configuration")
                                .animal(AnimalDTO.builder().animalType("SOME ANIMAL").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                ModifyConfigurationDTO.builder()
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
                        ModifyOfferDTO.builder()
                                .description("Second Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .offerConfigurations(
                                        new ArrayList<>(List.of(
                                                ModifyConfigurationDTO.builder()
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
                        ModifyOfferDTO.builder()
                                .description("Third Configuration")
                                .animal(AnimalDTO.builder().animalType("DOG").build())
                                .animalAmenities(new HashSet<>(List.of("not found amenity")))
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
            int configurationIndex, ModifyConfigurationDTO configurationToEdit, Class expectedExceptionClass) {

        Long configurationId = existingOffer.getOfferConfigurations().get(configurationIndex).getId();

        PersistenceUtils.addOfferConfigurationForOffer(existingOffer,
                List.of(animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                        "DOG", "SIZE", "SMALL").orElseThrow()),
                offerRepository);

        assertThrows(expectedExceptionClass,
                () -> offerService.editConfiguration(configurationId, configurationToEdit, caretakerWithComplexOffer.getEmail()));
    }

    static Stream<Arguments> provideIncorrectEditConfigurationScenarios() {
        return Stream.of(
                Arguments.of(
                        0, // ID of the configuration to edit
                        ModifyConfigurationDTO.builder()
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
        offerService.deleteConfiguration(configurationId, caretakerWithComplexOffer.getEmail());

        // Then
        Offer offerAfterDeletion = offerRepository.findById(existingOffer.getId()).orElseThrow();
        assertEquals(expectedNumberOfConfigurationsAfterDeletion, offerAfterDeletion.getOfferConfigurations().size());
    }

    @Test
    @Transactional
    void setAvailabilityForOffers_ShouldSetAvailabilityForOffer() {

        // Given
        CreateOffersAvailabilityDTO createOffersAvailabilityDTO = CreateOffersAvailabilityDTO.builder()
                .offerIds(List.of(existingOffer.getId()))
                .availabilityRanges(List.of(
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(1))
                                .availableTo(ZonedDateTime.now().plusDays(10))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(10))
                                .availableTo(ZonedDateTime.now().plusDays(20))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(25))
                                .availableTo(ZonedDateTime.now().plusDays(45))
                                .build()
                ))
                .build();

        // When
        offerService.setAvailabilityForOffers(createOffersAvailabilityDTO, caretakerWithComplexOffer.getEmail());

        // Then
        Offer offerAfterAvailabilitySet = offerRepository.findById(existingOffer.getId()).orElseThrow();
        assertEquals(3, offerAfterAvailabilitySet.getAvailabilities().size());

    }

    @Test
    @Transactional
    void setAvailabilityForOffers_whenSettingForMultipleOffers_ShouldSetAvailabilityForOffer() {

        // Given
        Offer anotherOffer = PersistenceUtils.addComplexOffer(
                caretakerWithComplexOffer, animalRepository.findById("CAT").orElseThrow(), List.of(),
                BigDecimal.valueOf(10.0), List.of(), Set.of(), offerRepository);

        CreateOffersAvailabilityDTO createOffersAvailabilityDTO = CreateOffersAvailabilityDTO.builder()
                .offerIds(List.of(existingOffer.getId(), anotherOffer.getId()))
                .availabilityRanges(List.of(
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(1))
                                .availableTo(ZonedDateTime.now().plusDays(10))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(10))
                                .availableTo(ZonedDateTime.now().plusDays(20))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(25))
                                .availableTo(ZonedDateTime.now().plusDays(45))
                                .build()
                ))
                .build();

        // When
        offerService.setAvailabilityForOffers(createOffersAvailabilityDTO, caretakerWithComplexOffer.getEmail());

        // Then
        Offer offerAfterAvailabilitySet = offerRepository.findById(existingOffer.getId()).orElseThrow();
        Offer anotherOfferAfterAvailabilitySet = offerRepository.findById(anotherOffer.getId()).orElseThrow();
        assertEquals(3, offerAfterAvailabilitySet.getAvailabilities().size());
        assertEquals(3, anotherOfferAfterAvailabilitySet.getAvailabilities().size());
    }

    @Test
    @Transactional
    void setAvailabilityForOffers_WhenOfferAlreadyHaveAvailabilities_ShouldReplaceAvailabilityForOffer() {

        // Given
        PersistenceUtils.setAvailabilitiesForOffer(offerRepository, existingOffer);

        CreateOffersAvailabilityDTO createOffersAvailabilityDTO = CreateOffersAvailabilityDTO.builder()
                .offerIds(List.of(existingOffer.getId()))
                .availabilityRanges(List.of(
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(1))
                                .availableTo(ZonedDateTime.now().plusDays(10))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(10))
                                .availableTo(ZonedDateTime.now().plusDays(20))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(25))
                                .availableTo(ZonedDateTime.now().plusDays(45))
                                .build()
                ))
                .build();

        // When
        offerService.setAvailabilityForOffers(createOffersAvailabilityDTO, caretakerWithComplexOffer.getEmail());

        // Then
        Offer offerAfterAvailabilitySet = offerRepository.findById(existingOffer.getId()).orElseThrow();
        assertEquals(3, offerAfterAvailabilitySet.getAvailabilities().size());

        availabilityRepository.deleteAll();

    }

    @ParameterizedTest
    @Transactional
    @MethodSource("provideIncorrectAvailabilityRanges")
    void setAvailabilityForOffers_whenProvidedAvailabilityRangesOverlapping_ShouldThrowIllegalArgumentException(
            List<AvailabilityRangeDTO> availabilityRanges) {

        CreateOffersAvailabilityDTO createOffersAvailabilityDTO = CreateOffersAvailabilityDTO.builder()
                .offerIds(List.of(existingOffer.getId()))
                .availabilityRanges(availabilityRanges)
                .build();

        assertThrows(AvailabilityDatesOverlappingException.class,
                () -> offerService.setAvailabilityForOffers(createOffersAvailabilityDTO, caretakerWithComplexOffer.getEmail()));

    }

    static Stream<Arguments> provideIncorrectAvailabilityRanges() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(1))
                                        .availableTo(ZonedDateTime.now().plusDays(10))
                                        .build(),
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(5))
                                        .availableTo(ZonedDateTime.now().plusDays(20))
                                        .build()
                        )
                ),
                Arguments.of(
                        List.of(
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(5))
                                        .availableTo(ZonedDateTime.now().plusDays(10))
                                        .build(),
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(2))
                                        .availableTo(ZonedDateTime.now().plusDays(7))
                                        .build()
                        )
                ),
                Arguments.of(
                        List.of(
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(5))
                                        .availableTo(ZonedDateTime.now().plusDays(10))
                                        .build(),
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(6))
                                        .availableTo(ZonedDateTime.now().plusDays(7))
                                        .build()
                        )
                ),
                Arguments.of(
                        List.of(
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(5))
                                        .availableTo(ZonedDateTime.now().plusDays(10))
                                        .build(),
                                AvailabilityRangeDTO.builder()
                                        .availableFrom(ZonedDateTime.now().plusDays(1))
                                        .availableTo(ZonedDateTime.now().plusDays(15))
                                        .build()
                        )
                )
        );
    }

    @Test
    @Transactional
    void setAvailabilityForOffers_whenModifyingByNotOwningCaretaker_ShouldThrowUnauthorizedException() {

        // Given
        CreateOffersAvailabilityDTO createOffersAvailabilityDTO = CreateOffersAvailabilityDTO.builder()
                .offerIds(List.of(existingOffer.getId()))
                .availabilityRanges(List.of(
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(1))
                                .availableTo(ZonedDateTime.now().plusDays(10))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(10))
                                .availableTo(ZonedDateTime.now().plusDays(20))
                                .build(),
                        AvailabilityRangeDTO.builder()
                                .availableFrom(ZonedDateTime.now().plusDays(25))
                                .availableTo(ZonedDateTime.now().plusDays(45))
                                .build()
                ))
                .build();

        // When Then
        assertThrows(UnauthorizedException.class,
                () -> offerService.setAvailabilityForOffers(createOffersAvailabilityDTO, "anotherCaretaker"));

    }

    @Test
    @Transactional
    void deleteAmenitiesFromOffer_ShouldDeleteAmenitiesFromOffer() {
        // Given
        PersistenceUtils.addConfigurationAndAmenitiesForOffer(
                existingOffer,
                List.of(),
                BigDecimal.valueOf(50.0),
                List.of(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow(),
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("garden", "DOG").orElseThrow()
                ),
                offerRepository
        );

        // When
        OfferDTO resultOfferDTO = offerService.deleteAmenitiesFromOffer(
                List.of("garden"),
                caretakerWithComplexOffer.getEmail(),
                existingOffer.getId()
        );

        // Then
        Offer offer = offerRepository.findById(resultOfferDTO.id()).orElseThrow();
        assertEquals(1, offer.getAnimalAmenities().size());

    }

    @Test
    void deleteAmenitiesFromOffer_whenNotOwningCaretakerDeleting_ShouldThrowUnauthorizedException() {

        // When Then
        assertThrows(UnauthorizedException.class,
                () -> offerService.deleteAmenitiesFromOffer(
                        List.of("garden"),
                        "badEmail",
                        existingOffer.getId()
                ));

    }

}

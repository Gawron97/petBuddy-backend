package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.availability.AvailabilityFilterDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferConfigurationFilterDTO;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.user.*;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.provider.geolocation.GeolocationProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.ReflectionUtils.getPrimitiveNames;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CaretakerServiceIntegrationTest {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private AnimalAttributeRepository animalAttributeRepository;

    @Autowired
    private AnimalAmenityRepository animalAmenityRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerService caretakerService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CareRepository careRepository;

    @MockBean
    private GeolocationProvider geolocationProvider;

    private Caretaker caretaker;
    private Client clientSameAsCaretaker;
    private Client client;


    @BeforeEach
    void init() {
        initCaretakers();
        initClients(this.caretaker);
        when(geolocationProvider.getCoordinatesOfAddress(anyString(), anyString(), anyString()))
                .thenReturn(createMockCoordinates());
    }

    private void initCaretakers() {
        List<Caretaker> caretakers = PersistenceUtils.addCaretakers(caretakerRepository, appUserRepository);
        PersistenceUtils.addOffersToCaretakers(caretakers, offerRepository, animalRepository.findAll());
        this.caretaker = caretakers.get(0);
    }

    private void initClients(Caretaker caretaker) {
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);

        clientSameAsCaretaker = Client.builder()
                .email(caretaker.getEmail())
                .accountData(caretaker.getAccountData())
                .build();

        clientSameAsCaretaker = PersistenceUtils.addClient(appUserRepository, clientRepository, clientSameAsCaretaker);
    }

    @AfterEach
    void cleanUp() {
        ratingRepository.deleteAll();
        careRepository.deleteAll();
        offerRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    private void createCaretakersWithComplexOffers() {
        // Opiekun 1
        Caretaker caretaker1 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("John", "Doe", "john.doe@example.com", createMockAddress()));

        // Oferta 1 dla Opiekuna 1 - DOG, SIZE: BIG, SEX: MALE, cena 10.0, udogodnienia: toys
        PersistenceUtils.addComplexOffer(
                caretaker1,
                animalRepository.findById("DOG").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SIZE", "BIG").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SEX", "MALE").orElseThrow()
                ),
                BigDecimal.valueOf(10.0),
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                Set.of(
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 1, 1))
                                .availableTo(LocalDate.of(2025, 1, 10))
                                .build(),
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 2, 1))
                                .availableTo(LocalDate.of(2025, 2, 10))
                                .build()
                ),
                offerRepository
        );

        // Oferta 2 dla Opiekuna 1 - CAT, SIZE: BIG, cena 30.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker1,
                animalRepository.findById("CAT").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("CAT", "SIZE", "BIG").orElseThrow()
                ),
                BigDecimal.valueOf(30.0),
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("scratching post", "CAT").orElseThrow(),
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "CAT").orElseThrow()
                ),
                Set.of(
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 1, 5))
                                .availableTo(LocalDate.of(2025, 1, 15))
                                .build(),
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 2, 5))
                                .availableTo(LocalDate.of(2025, 2, 15))
                                .build()
                ),
                offerRepository
        );

        // Opiekun 2
        Caretaker caretaker2 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("Jane", "Smith", "jane.smith@example.com", createMockAddress()));

        // Oferta 1 dla Opiekuna 2 - DOG, SIZE: SMALL, cena 20.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker2,
                animalRepository.findById("DOG").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SIZE", "SMALL").orElseThrow()
                ),
                BigDecimal.valueOf(20.0),
                Arrays.asList(),
                Set.of(
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 1, 7))
                                .availableTo(LocalDate.of(2025, 1, 10))
                                .build(),
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 2, 8))
                                .availableTo(LocalDate.of(2025, 2, 10))
                                .build()
                ),
                offerRepository
        );

        // Oferta 2 dla Opiekuna 2 - BIRD, SIZE: SMALL, cena 15.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker2,
                animalRepository.findById("BIRD").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("BIRD", "SIZE", "SMALL").orElseThrow()
                ),
                BigDecimal.valueOf(15.0),
                Arrays.asList(),
                Set.of(),
                offerRepository
        );

        // Opiekun 3
        Caretaker caretaker3 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("Alice", "Brown", "alice.brown@example.com", createMockAddress()));

        // Oferta 1 dla Opiekuna 3 - FISH, AQUARIUM: YES, SEX: MALE, cena 12.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker3,
                animalRepository.findById("FISH").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("FISH", "AQUARIUM", "YES").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("FISH", "SEX", "MALE").orElseThrow()
                ),
                BigDecimal.valueOf(12.0),
                Arrays.asList(),
                Set.of(
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 3, 1))
                                .availableTo(LocalDate.of(2025, 3, 10))
                                .build(),
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 4, 1))
                                .availableTo(LocalDate.of(2025, 4, 10))
                                .build()
                ),
                offerRepository
        );

        // Oferta 2 dla Opiekuna 3 - REPTILE, DANGEROUS: NO, SEX: SHE, cena 40.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker3,
                animalRepository.findById("REPTILE").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("REPTILE", "DANGEROUS", "NO").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("REPTILE", "SEX", "SHE").orElseThrow()
                ),
                BigDecimal.valueOf(40.0),
                Arrays.asList(),
                Set.of(),
                offerRepository
        );

        // Opiekun 4
        Caretaker caretaker4 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("Bob", "Johnson", "bob.johnson@example.com", createMockAddress()));

        // Oferta 1 dla Opiekuna 4 - HORSE, SIZE: BIG, SEX: MALE, cena 50.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker4,
                animalRepository.findById("HORSE").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("HORSE", "SIZE", "BIG").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("HORSE", "SEX", "MALE").orElseThrow()
                ),
                BigDecimal.valueOf(50.0),
                Arrays.asList(),
                Set.of(),
                offerRepository
        );

        // Oferta 2 dla Opiekuna 4 - CAT, SEX: SHE, cena 18.0, udogodnienia: scratching post
        PersistenceUtils.addComplexOffer(
                caretaker4,
                animalRepository.findById("CAT").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("CAT", "SEX", "SHE").orElseThrow()
                ),
                BigDecimal.valueOf(18.0),
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("scratching post", "CAT").orElseThrow()
                ),
                Set.of(
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 1, 1))
                                .availableTo(LocalDate.of(2025, 1, 5))
                                .build()
                ),
                offerRepository
        );

        // Opiekun 5
        Caretaker caretaker5 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("Charlie", "Lee", "charlie.lee@example.com", createMockAddress()));

        // Oferta 1 dla Opiekuna 5 - DOG, SIZE: BIG, SEX: MALE, cena 35.0, udogodnienia: toys
        PersistenceUtils.addComplexOffer(
                caretaker5,
                animalRepository.findById("DOG").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SIZE", "BIG").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SIZE", "SMALL").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SEX", "MALE").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("DOG", "SEX", "SHE").orElseThrow()
                ),
                BigDecimal.valueOf(35.0),
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                Set.of(
                        Availability.builder()
                                .availableFrom(LocalDate.of(2025, 8, 1))
                                .availableTo(LocalDate.of(2025, 8, 10))
                                .build()
                ),
                offerRepository
        );

        // Opiekun 6
        Caretaker caretaker6 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("Emily", "Davis", "emily.davis@example.com", createMockAddress()));

        // Oferta 1 dla Opiekuna 6 - FISH, AQUARIUM: NO, SEX: SHE, cena 8.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker6,
                animalRepository.findById("FISH").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("FISH", "AQUARIUM", "NO").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("FISH", "SEX", "SHE").orElseThrow()
                ),
                BigDecimal.valueOf(8.0),
                Arrays.asList(),
                Set.of(),
                offerRepository
        );

        // Oferta 2 dla Opiekuna 6 - BIRD, SIZE: BIG, SEX: MALE, cena 22.0, bez udogodnień
        PersistenceUtils.addComplexOffer(
                caretaker6,
                animalRepository.findById("BIRD").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("BIRD", "SIZE", "BIG").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue("BIRD", "SEX", "MALE").orElseThrow()
                ),
                BigDecimal.valueOf(22.0),
                Arrays.asList(),
                Set.of(),
                offerRepository
        );
    }


    @ParameterizedTest
    @MethodSource("provideFilterParams")
    void getCaretakersWithFiltering_shouldReturnFilteredResults(
            CaretakerSearchCriteria filters,
            Set<OfferFilterDTO> offerFilters,
            int expectedSize
    ) {

        appUserRepository.deleteAll();
        createCaretakersWithComplexOffers();
        Page<CaretakerDTO> resultPage = caretakerService.getCaretakers(Pageable.ofSize(10), filters, offerFilters);
        assertEquals(expectedSize, resultPage.getContent().size());

    }

    private static Stream<Arguments> provideFilterParams() {
        return Stream.of(
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(null)
                                        .build()
                        ),
                        3 // Caretakers: John Doe, Jane Smith, Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(null)
                                        .build(),
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(null)
                                        .build()
                        ),
                        1 // Caretakers: John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(null)
                                        .build()
                        ),
                        2 // Caretakers: John Doe, Bob Johnson
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("FISH")
                                        .offerConfigurations(null)
                                        .build()
                        ),
                        2 // Caretakers: Alice Brown, Emily Davis
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SIZE", Set.of("BIG")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        2 // Caretakers: John Doe, Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SIZE", Set.of("SMALL")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        2 // Caretakers: Jane Smith, Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of(
                                                                "SIZE", Set.of("BIG"),
                                                                "SEX", Set.of("MALE"))
                                                        )
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        2 // Caretakers: John Doe, Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of(
                                                                "SIZE", Set.of("SMALL"),
                                                                "SEX", Set.of("SHE"))
                                                        )
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        2 // Caretakers: Jane Smith, Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("FISH")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("AQUARIUM", Set.of("YES")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Alice Brown
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("FISH")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("AQUARIUM", Set.of("NO")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Emily Davis
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(null)
                                                        .minPrice(BigDecimal.valueOf(10.0))
                                                        .maxPrice(BigDecimal.valueOf(20.0))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        2 // Caretakers: John Doe, Jane Smith
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SIZE", Set.of("BIG")))
                                                        .minPrice(BigDecimal.valueOf(30.0))
                                                        .maxPrice(BigDecimal.valueOf(40.0))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().personalDataLike("Doe").build(),
                        Collections.emptySet(),
                        1 // Caretaker: John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().personalDataLike("Smith").build(),
                        Collections.emptySet(),
                        1 // Caretaker: Jane Smith
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("REPTILE")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("DANGEROUS", Set.of("YES")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        0 // No caretakers offer REPTILE with DANGEROUS: YES
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("REPTILE")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("DANGEROUS", Set.of("NO")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Alice Brown
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("BIRD")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(null)
                                                        .minPrice(BigDecimal.valueOf(10.0))
                                                        .maxPrice(BigDecimal.valueOf(20.0))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Jane Smith
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("BIRD")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(null)
                                                        .minPrice(BigDecimal.valueOf(20.0))
                                                        .maxPrice(BigDecimal.valueOf(25.0))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Emily Davis
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SEX", Set.of("MALE")))
                                                        .minPrice(null)
                                                        .maxPrice(BigDecimal.valueOf(15.0))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SEX", Set.of("MALE")))
                                                        .minPrice(BigDecimal.valueOf(20.0))
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        2 // Caretaker: Charlie Lee, Jane Smith
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().personalDataLike("Brown").build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("FISH")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("AQUARIUM", Set.of("YES")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: Alice Brown
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("BIRD")
                                        .offerConfigurations(null)
                                        .build()
                        ),
                        2 // Caretakers: Jane Smith, Emily Davis
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SIZE", Set.of("BIG")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build(),
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SEX", Set.of("SHE")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretaker: John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of(
                                                                "SIZE", Set.of("BIG", "SMALL"),
                                                                "SEX", Set.of("MALE", "SHE")
                                                        ))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // Caretakers: Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("toys"))
                                        .build()
                        ),
                        2 // Caretakers: John Doe, Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("scratching post"))
                                        .build()
                        ),
                        2 // Caretakers: John Doe, Bob Johnson
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("toys", "scratching post"))
                                        .build()
                        ),
                        1 // Caretakers: John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("toys"))
                                        .build(),
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("scratching post"))
                                        .build()
                        ),
                        1 // Caretakers: John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of(
                                                                "SIZE", Set.of("BIG", "SMALL"),
                                                                "SEX", Set.of("MALE", "SHE")
                                                        ))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("toys"))
                                        .build()
                        ),
                        1 // Caretakers: Charlie Lee
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .amenities(Set.of("toys"))
                                        .build(),
                                OfferFilterDTO.builder()
                                        .animalType("BIRD")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .build()
                        ),
                        0
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 1, 1))
                                                        .availableTo(LocalDate.of(2025, 1, 7))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 1, 8))
                                                        .availableTo(LocalDate.of(2025, 1, 10))
                                                        .build(),
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2028, 8, 8))
                                                        .availableTo(LocalDate.of(2028, 8, 10))
                                                        .build()
                                        ))
                                        .build(),
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 1, 1))
                                                        .availableTo(LocalDate.of(2025, 1, 10))
                                                        .build(),
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2028, 8, 8))
                                                        .availableTo(LocalDate.of(2028, 8, 10))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        1 // John Doe
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of("SIZE", Set.of("SMALL")))
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 1, 3))
                                                        .availableTo(LocalDate.of(2025, 1, 10))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        0
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("CAT")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 8, 1))
                                                        .availableTo(LocalDate.of(2025, 8, 10))
                                                        .build()
                                        ))
                                        .build(),
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 1, 1))
                                                        .availableTo(LocalDate.of(2025, 1, 10))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        0
                ),
                Arguments.of(
                        CaretakerSearchCriteria.builder().build(),
                        Set.of(
                                OfferFilterDTO.builder()
                                        .animalType("DOG")
                                        .offerConfigurations(Set.of(
                                                OfferConfigurationFilterDTO.builder()
                                                        .attributes(Map.of())
                                                        .minPrice(null)
                                                        .maxPrice(null)
                                                        .build()
                                        ))
                                        .availabilities(Set.of(
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 1, 7))
                                                        .availableTo(LocalDate.of(2025, 1, 10))
                                                        .build(),
                                                AvailabilityFilterDTO.builder()
                                                        .availableFrom(LocalDate.of(2025, 8, 1))
                                                        .availableTo(LocalDate.of(2025, 8, 10))
                                                        .build()
                                        ))
                                        .build()
                        ),
                        3 // John Doe, Jane Smith Charlie Lee
                )
        );
    }


    @Test
    void getCaretakers_shouldReturnProperRatingNumberAndAverageRating() {

        // Given
        Client client2 = PersistenceUtils.addClient(appUserRepository, clientRepository,
                createMockClient("secondClient", "seconfClient", "secondClientEmail"));

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        Care care2 = PersistenceUtils.addCare(careRepository, caretaker, client2, animalRepository.findById("CAT").get());

        PersistenceUtils.addRatingToCaretaker(caretaker, client, care, 5, "comment", ratingRepository);
        PersistenceUtils.addRatingToCaretaker(caretaker, client2, care2, 4, "comment second", ratingRepository);

        // When
        Page<CaretakerDTO> resultPage = caretakerService.getCaretakers(
                Pageable.ofSize(10),
                CaretakerSearchCriteria.builder()
                        .personalDataLike("John Doe")
                        .build(),
                Collections.emptySet()
        );
        CaretakerDTO resultCaretaker = resultPage.getContent().get(0);
        assertEquals(2, resultCaretaker.numberOfRatings());
        assertEquals(4.5f, resultCaretaker.avgRating());

    }

    @Test
    void testGetCaretakers_sortingParamsShouldAlignWithDTO() {
        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(CaretakerDTO.class);
        fieldNames.addAll(getPrimitiveNames(AddressDTO.class, "address_"));
        fieldNames.addAll(getPrimitiveNames(AccountDataDTO.class, "accountData_"));
        fieldNames.remove("availabilityDaysMatch");

        for(String fieldName : fieldNames) {
            assertDoesNotThrow(() -> caretakerService.getCaretakers(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    CaretakerSearchCriteria.builder().build(),
                    Collections.emptySet()
            ));
        }
    }

    @Test
    @Transactional
    void addCaretaker_whenUserExistsButCaretakerProfileNot_shouldCreateCaretakerProperly() {

        //Given
        AppUser appUser = PersistenceUtils.addAppUser(appUserRepository);

        ModifyCaretakerDTO caretakerToCreate = ModifyCaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("description")
                .address(
                        AddressDTO.builder()
                                .city("city")
                                .zipCode("zipCode")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .streetNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When
        CaretakerComplexDTO result = caretakerService.addCaretaker(caretakerToCreate, appUser.getEmail(), new ArrayList<>());
        Caretaker caretaker = caretakerRepository.findById(result.accountData().email()).orElse(null);

        //Then
        assertNotNull(caretaker);
        assertEquals(caretakerToCreate.phoneNumber(), caretaker.getPhoneNumber());
        assertEquals(caretakerToCreate.description(), caretaker.getDescription());
        assertEquals(caretakerToCreate.address().city(), caretaker.getAddress().getCity());
        assertEquals(caretakerToCreate.address().zipCode(), caretaker.getAddress().getZipCode());
        assertEquals(caretakerToCreate.address().voivodeship(), caretaker.getAddress().getVoivodeship());
        assertEquals(caretakerToCreate.address().street(), caretaker.getAddress().getStreet());
        assertEquals(caretakerToCreate.address().streetNumber(), caretaker.getAddress().getStreetNumber());
        assertEquals(caretakerToCreate.address().apartmentNumber(), caretaker.getAddress().getApartmentNumber());


    }

    @Test
    @Transactional
    void addCaretaker_whenUserNotExists_shouldThrowException() {

        //Given
        ModifyCaretakerDTO caretakerToCreate = ModifyCaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("description")
                .address(
                        AddressDTO.builder()
                                .city("city")
                                .zipCode("zipCode")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .streetNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When Then
        assertThrows(NotFoundException.class,
                () -> caretakerService.addCaretaker(caretakerToCreate, "Not existing email", new ArrayList<>()));

    }

    @Test
    @Transactional
    void editCaretaker_whenCaretakerProfileExistsWithOffers_shouldEditOnlyProvidedProfileDataAndRemainRestData() {

        //Given
        Caretaker caretakerWithComplexOffer = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        PersistenceUtils.addComplexOffer(
                caretakerWithComplexOffer,
                animalRepository.findById("DOG").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                                "DOG", "SIZE", "BIG").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                                "DOG", "SEX", "MALE").orElseThrow()
                ),
                BigDecimal.valueOf(10.0),
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                Set.of(),
                offerRepository);

        ModifyCaretakerDTO caretakerToCreate = ModifyCaretakerDTO.builder()
                .phoneNumber("123")
                .description("description")
                .address(
                        AddressDTO.builder()
                                .city("city")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .zipCode("12-321")
                                .street("street")
                                .streetNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When
        CaretakerComplexDTO result = caretakerService.editCaretaker(
                caretakerToCreate,
                caretakerWithComplexOffer.getEmail(),
                Collections.emptySet(),
                Collections.emptyList()
        );
        Caretaker caretaker = caretakerRepository.findById(result.accountData().email()).orElse(null);

        //Then
        assertNotNull(caretaker);
        assertEquals("123", caretaker.getPhoneNumber());
        assertEquals(caretakerToCreate.description(), caretaker.getDescription());
        assertEquals(caretakerToCreate.address().city(), caretaker.getAddress().getCity());
        assertEquals("12-321", caretaker.getAddress().getZipCode());
        assertEquals(caretakerToCreate.address().voivodeship(), caretaker.getAddress().getVoivodeship());
        assertEquals(caretakerToCreate.address().street(), caretaker.getAddress().getStreet());
        assertEquals(caretakerToCreate.address().streetNumber(), caretaker.getAddress().getStreetNumber());
        assertEquals(caretakerToCreate.address().apartmentNumber(), caretaker.getAddress().getApartmentNumber());
        assertEquals(1, caretaker.getOffers().size());
        assertEquals(1, caretaker.getOffers().get(0).getAnimalAmenities().size());

        Offer offerToDelete = offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerWithComplexOffer.getEmail(), "DOG").orElseThrow();
        offerRepository.delete(offerToDelete);
        caretakerWithComplexOffer.setOffers(null);
        caretakerRepository.delete(caretakerWithComplexOffer);

    }

    @Test
    @Transactional
    void editCaretaker_whenCaretakerProfileExistsWithOffers_shouldEditProfileDataAndRemainRestData() {

        //Given
        Caretaker caretakerWithComplexOffer = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        PersistenceUtils.addComplexOffer(
                caretakerWithComplexOffer,
                animalRepository.findById("DOG").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                                "DOG", "SIZE", "BIG").orElseThrow(),
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                                "DOG", "SEX", "MALE").orElseThrow()
                ),
                BigDecimal.valueOf(10.0),
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                Set.of(),
                offerRepository);

        ModifyCaretakerDTO caretakerToCreate = ModifyCaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("description")
                .address(
                        AddressDTO.builder()
                                .city("city")
                                .zipCode("zipCode")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .streetNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When
        CaretakerComplexDTO result = caretakerService.editCaretaker(
                caretakerToCreate,
                caretakerWithComplexOffer.getEmail(),
                Collections.emptySet(),
                Collections.emptyList()
        );
        Caretaker caretaker = caretakerRepository.findById(result.accountData().email()).orElse(null);

        //Then
        assertNotNull(caretaker);
        assertEquals(caretakerToCreate.phoneNumber(), caretaker.getPhoneNumber());
        assertEquals(caretakerToCreate.description(), caretaker.getDescription());
        assertEquals(caretakerToCreate.address().city(), caretaker.getAddress().getCity());
        assertEquals(caretakerToCreate.address().zipCode(), caretaker.getAddress().getZipCode());
        assertEquals(caretakerToCreate.address().voivodeship(), caretaker.getAddress().getVoivodeship());
        assertEquals(caretakerToCreate.address().street(), caretaker.getAddress().getStreet());
        assertEquals(caretakerToCreate.address().streetNumber(), caretaker.getAddress().getStreetNumber());
        assertEquals(caretakerToCreate.address().apartmentNumber(), caretaker.getAddress().getApartmentNumber());
        assertEquals(1, caretaker.getOffers().size());
        assertEquals(1, caretaker.getOffers().get(0).getAnimalAmenities().size());

        Offer offerToDelete = offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerWithComplexOffer.getEmail(), "DOG").orElseThrow();
        offerRepository.delete(offerToDelete);
        caretakerWithComplexOffer.setOffers(null);
        caretakerRepository.delete(caretakerWithComplexOffer);

    }

    @Test
    @Transactional
    void getOtherCaretaker_shouldReturnProperCaretaker() {

        //Given
        String email = "testmail@mail.com";

        //When
        CaretakerComplexPublicDTO result = caretakerService.getOtherCaretaker(email);

        //Then
        assertNotNull(result);
        assertEquals(email, result.accountData().email());

    }

    @Test
    void getOtherCaretaker_whenCaretakerNotExists_shouldThrowNotFoundException() {

        //Given
        String email = "notexists@mail.com";

        //When Then
        assertThrows(NotFoundException.class, () -> caretakerService.getOtherCaretaker(email));

    }

    @Test
    @Transactional
    void getMyCaretakerProfile_shouldReturnProperAnswer() {

        //Given
        String email = "testmail@mail.com";

        //When
        CaretakerComplexDTO result = caretakerService.getMyCaretakerProfile(email);

        //Then
        assertNotNull(result);
        assertEquals(email, result.accountData().email());

    }
}

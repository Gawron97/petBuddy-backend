package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.config.TestDataConfiguration;
import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.address.UpdateAddressDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.OfferSearchCriteria;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.MockUtils.createMockRating;
import static com.example.petbuddybackend.testutils.ReflectionUtils.getPrimitiveNames;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CaretakerServiceTest {

    @MockBean
    private JwtDecoder jwtDecoder;

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

    private Caretaker caretaker;
    private Client clientSameAsCaretaker;
    private Client client;


    @BeforeEach
    void init() {
        initCaretakers();
        initClients(this.caretaker);
    }

    @AfterEach
    void cleanUp() {
        offerRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("provideSpecificationParams")
    void getCaretakersWithFiltering_shouldReturnFilteredResults(
            CaretakerSearchCriteria filters,
            int expectedSize
    ) {

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
                10.0,
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                offerRepository);

        PersistenceUtils.addComplexOffer(
                caretakerWithComplexOffer,
                animalRepository.findById("DOG").orElseThrow(),
                Arrays.asList(
                        animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                                "DOG", "SIZE", "SMALL").orElseThrow()
                ),
                90.0,
                List.of(),
                offerRepository,
                caretakerWithComplexOffer.getOffers().get(0));

        Page<CaretakerDTO> resultPage = caretakerService.getCaretakers(Pageable.ofSize(10), filters);

        assertEquals(expectedSize, resultPage.getContent().size());

        Offer offerToDelete = offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerWithComplexOffer.getEmail(), "DOG").orElseThrow();
        offerRepository.delete(offerToDelete);
        caretakerWithComplexOffer.setOffers(null);
        caretakerRepository.delete(caretakerWithComplexOffer);

    }

    @Test
    void testGetCaretakers_sortingParamsShouldAlignWithDTO() {
        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(CaretakerDTO.class);
        fieldNames.addAll(getPrimitiveNames(AddressDTO.class, "address_"));
        fieldNames.addAll(getPrimitiveNames(AccountDataDTO.class, "accountData_"));

        for(String fieldName : fieldNames) {
            assertDoesNotThrow(() -> caretakerService.getCaretakers(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    CaretakerSearchCriteria.builder().build()
            ));
        }
    }

    @Test
    void testGetRating_sortingParamsShouldAlignWithDTO() {
        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(RatingResponse.class);

        for(String fieldName : fieldNames) {
            assertDoesNotThrow(() -> caretakerService.getRatings(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    caretaker.getEmail()
            ));
        }
    }

    @Test
    @Transactional
    void rateCaretaker_shouldSucceed() throws IllegalAccessException {
        caretakerService.rateCaretaker(
                caretaker.getEmail(),
                client.getAccountData().getEmail(),
                5,
                "comment"
        );

        Rating rating = ratingRepository.getReferenceById(new RatingKey(caretaker.getEmail(), client.getEmail()));
        assertEquals(1, ratingRepository.count());
        assertEquals(5, rating.getRating());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(rating, Set.of("client", "caretaker")));
    }

    @Test
    @Transactional
    void rateCaretaker_ratingExists_shouldUpdateRating() {
        ratingRepository.save(createMockRating(caretaker, client));

        caretakerService.rateCaretaker(
                caretaker.getEmail(),
                client.getAccountData().getEmail(),
                5,
                "new comment"
        );

        Rating rating = ratingRepository.getReferenceById(new RatingKey(caretaker.getEmail(), client.getEmail()));
        assertEquals(1, ratingRepository.count());
        assertEquals(5, rating.getRating());
        assertEquals("new comment", rating.getComment());
        assertEquals(client.getEmail(), rating.getClientEmail());
        assertEquals(caretaker.getEmail(), rating.getCaretakerEmail());
    }

    @ParameterizedTest
    @MethodSource("provideRatingParams")
    void rateCaretaker_invalidRating_(int rating, boolean shouldSucceed) {
        if(shouldSucceed) {
            caretakerService.rateCaretaker(
                    caretaker.getEmail(),
                    client.getAccountData().getEmail(),
                    rating,
                    "comment"
            );
            return;
        }

        assertThrows(DataIntegrityViolationException.class, () -> caretakerService.rateCaretaker(
                caretaker.getEmail(),
                client.getAccountData().getEmail(),
                rating,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_clientRatesHimself_shouldThrowIllegalActionException() {
        assertThrows(IllegalActionException.class, () -> caretakerService.rateCaretaker(
                caretaker.getEmail(),
                clientSameAsCaretaker.getAccountData().getEmail(),
                5,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_caretakerDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> caretakerService.rateCaretaker(
                "invalidEmail",
                client.getAccountData().getEmail(),
                5,
                "comment"
        ));
    }

    @Test
    void deleteRating_shouldSucceed() {
        ratingRepository.saveAndFlush(createMockRating(caretaker, client));

        caretakerService.deleteRating(caretaker.getEmail(), client.getAccountData().getEmail());
        assertEquals(0, ratingRepository.count());
    }

    @Test
    void deleteRating_caretakerDoesNotExist_shouldThrow() {
        assertThrows(NotFoundException.class, () -> caretakerService.deleteRating(
                "invalidEmail",
                client.getAccountData().getEmail()
        ));
    }

    @Test
    void deleteRating_ratingDoesNotExist_shouldThrow() {
        assertThrows(NotFoundException.class, () -> caretakerService.deleteRating(
                caretaker.getEmail(),
                client.getAccountData().getEmail()
        ));
    }

    @Test
    void getRatings_shouldReturnRatings() {
        ratingRepository.saveAndFlush(createMockRating(caretaker, client));

        Page<RatingResponse> ratings = caretakerService.getRatings(Pageable.ofSize(10), caretaker.getEmail());
        assertEquals(1, ratings.getContent().size());
    }

    @Test
    void getRating_shouldReturnRating() {
        Rating rating = createMockRating(caretaker, client);
        ratingRepository.saveAndFlush(rating);

        Rating foundRating = caretakerService.getRating(caretaker.getEmail(), client.getEmail());
        assertEquals(rating.getCaretakerEmail(), foundRating.getCaretakerEmail());
        assertEquals(rating.getClientEmail(), foundRating.getClientEmail());
    }

    @Test
    void getRating_ratingDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> caretakerService.getRating(
                caretaker.getEmail(),
                client.getEmail()
        ));
    }

    private static Stream<Arguments> provideRatingParams() {
        return Stream.of(
                Arguments.of(-1, false),
                Arguments.of(0, false),
                Arguments.of(1, true),
                Arguments.of(2, true),
                Arguments.of(3, true),
                Arguments.of(4, true),
                Arguments.of(5, true),
                Arguments.of(6, false)
        );
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

    private static Stream<Arguments> provideSpecificationParams() {
        return Stream.of(
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("DOG"))
                                .build()
                ).build(), 3),
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("CAT"))
                                .build()
                ).build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("DOG", "CAT"))
                                .build()
                ).build(), 3),
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("DOG", "CAT"))
                                .amenities(Set.of("toys"))
                                .build()
                ).build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("DOG", "CAT"))
                                .minPrice(10.0)
                                .maxPrice(10.0)
                                .build()
                ).build(), 3),
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("DOG"))
                                .animalAttributeIds(Set.of(1L))
                                .minPrice(10.0)
                                .maxPrice(10.0)
                                .build()
                ).build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().offerSearchCriteria(
                        OfferSearchCriteria.builder()
                                .animalTypes(Set.of("DOG"))
                                .animalAttributeIds(Set.of(1L))
                                .minPrice(80.0)
                                .maxPrice(100.0)
                                .build()
                ).build(), 0),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("doe").build(), 2),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("testmail").build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("john   doe").build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("doe  john   ").build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike(" ").build(), 4),
                Arguments.of(CaretakerSearchCriteria.builder().build(), 4),
                Arguments.of(CaretakerSearchCriteria.builder().voivodeship(Voivodeship.SLASKIE).build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().cityLike("war").build(), 3)
        );
    }


    @Test
    @Transactional
    void addCaretaker_whenUserExistsButCaretakerProfileNot_shouldCreateCaretakerProperly() {

        //Given
        AppUser appUser = PersistenceUtils.addAppUser(appUserRepository);

        CreateCaretakerDTO caretakerToCreate = CreateCaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("description")
                .address(
                        AddressDTO.builder()
                                .city("city")
                                .zipCode("zipCode")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .buildingNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When
        CaretakerDTO result = caretakerService.addCaretaker(caretakerToCreate, appUser.getEmail());
        Caretaker caretaker = caretakerRepository.findById(result.accountData().email()).orElse(null);

        //Then
        assertNotNull(caretaker);
        assertEquals(caretakerToCreate.phoneNumber(), caretaker.getPhoneNumber());
        assertEquals(caretakerToCreate.description(), caretaker.getDescription());
        assertEquals(caretakerToCreate.address().city(), caretaker.getAddress().getCity());
        assertEquals(caretakerToCreate.address().zipCode(), caretaker.getAddress().getZipCode());
        assertEquals(caretakerToCreate.address().voivodeship(), caretaker.getAddress().getVoivodeship());
        assertEquals(caretakerToCreate.address().street(), caretaker.getAddress().getStreet());
        assertEquals(caretakerToCreate.address().buildingNumber(), caretaker.getAddress().getBuildingNumber());
        assertEquals(caretakerToCreate.address().apartmentNumber(), caretaker.getAddress().getApartmentNumber());


    }

    @Test
    @Transactional
    void addCaretaker_whenUserNotExists_shouldThrowException() {

        //Given
        CreateCaretakerDTO caretakerToCreate = CreateCaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("description")
                .address(
                        AddressDTO.builder()
                                .city("city")
                                .zipCode("zipCode")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .buildingNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When Then
        assertThrows(NotFoundException.class, () -> caretakerService.addCaretaker(caretakerToCreate, "Not existing email"));

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
                10.0,
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                offerRepository);

        UpdateCaretakerDTO caretakerToCreate = UpdateCaretakerDTO.builder()
                .phoneNumber("")
                .description("description")
                .address(
                        UpdateAddressDTO.builder()
                                .city("city")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .buildingNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        String oldPhoneNumber = caretakerWithComplexOffer.getPhoneNumber();
        String oldZipCode = caretakerWithComplexOffer.getAddress().getZipCode();

        //When
        CaretakerDTO result = caretakerService.editCaretaker(caretakerToCreate, caretakerWithComplexOffer.getEmail());
        Caretaker caretaker = caretakerRepository.findById(result.accountData().email()).orElse(null);

        //Then
        assertNotNull(caretaker);
        assertEquals(oldPhoneNumber, caretaker.getPhoneNumber());
        assertEquals(caretakerToCreate.description(), caretaker.getDescription());
        assertEquals(caretakerToCreate.address().city(), caretaker.getAddress().getCity());
        assertEquals(oldZipCode, caretaker.getAddress().getZipCode());
        assertEquals(caretakerToCreate.address().voivodeship(), caretaker.getAddress().getVoivodeship());
        assertEquals(caretakerToCreate.address().street(), caretaker.getAddress().getStreet());
        assertEquals(caretakerToCreate.address().buildingNumber(), caretaker.getAddress().getBuildingNumber());
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
                10.0,
                Arrays.asList(
                        animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType("toys", "DOG").orElseThrow()
                ),
                offerRepository);

        UpdateCaretakerDTO caretakerToCreate = UpdateCaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("description")
                .address(
                        UpdateAddressDTO.builder()
                                .city("city")
                                .zipCode("zipCode")
                                .voivodeship(Voivodeship.DOLNOSLASKIE)
                                .street("street")
                                .buildingNumber("33HHD")
                                .apartmentNumber("150SD")
                                .build()
                )
                .build();

        //When
        CaretakerDTO result = caretakerService.editCaretaker(caretakerToCreate, caretakerWithComplexOffer.getEmail());
        Caretaker caretaker = caretakerRepository.findById(result.accountData().email()).orElse(null);

        //Then
        assertNotNull(caretaker);
        assertEquals(caretakerToCreate.phoneNumber(), caretaker.getPhoneNumber());
        assertEquals(caretakerToCreate.description(), caretaker.getDescription());
        assertEquals(caretakerToCreate.address().city(), caretaker.getAddress().getCity());
        assertEquals(caretakerToCreate.address().zipCode(), caretaker.getAddress().getZipCode());
        assertEquals(caretakerToCreate.address().voivodeship(), caretaker.getAddress().getVoivodeship());
        assertEquals(caretakerToCreate.address().street(), caretaker.getAddress().getStreet());
        assertEquals(caretakerToCreate.address().buildingNumber(), caretaker.getAddress().getBuildingNumber());
        assertEquals(caretakerToCreate.address().apartmentNumber(), caretaker.getAddress().getApartmentNumber());
        assertEquals(1, caretaker.getOffers().size());
        assertEquals(1, caretaker.getOffers().get(0).getAnimalAmenities().size());

        Offer offerToDelete = offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerWithComplexOffer.getEmail(), "DOG").orElseThrow();
        offerRepository.delete(offerToDelete);
        caretakerWithComplexOffer.setOffers(null);
        caretakerRepository.delete(caretakerWithComplexOffer);

    }


}

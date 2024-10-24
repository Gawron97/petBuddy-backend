package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import com.example.petbuddybackend.utils.exception.throweable.InvalidRoleException;
import com.example.petbuddybackend.utils.exception.throweable.StateTransitionException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockClient;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CareServiceIntegrationTest {

    @Autowired
    private CareRepository careRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CareService careService;

    private Caretaker caretaker;
    private Client client;

    @BeforeEach
    public void setUp() {
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
    }

    @AfterEach
    public void tearDown() {
        careRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void makeReservation_ShouldReturnProperCreatedCare() {

        // Given
        CreateCareDTO createCareDTO = CreateCareDTO.builder()
                .careStart(LocalDate.now().plusDays(1))
                .careEnd(LocalDate.now().plusDays(5))
                .description("Description")
                .dailyPrice(new BigDecimal("10.00"))
                .animalType("DOG")
                .animalAttributeIds(new ArrayList<>())
                .build();

        // When
        CareDTO result = careService.makeReservation(createCareDTO, client.getEmail(), caretaker.getEmail(), ZoneId.systemDefault());

        // Then
        Care care = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(care);
        assertEquals(CareStatus.PENDING, care.getCaretakerStatus());
        assertEquals(CareStatus.ACCEPTED, care.getClientStatus());
        assertEquals(createCareDTO.careStart(), care.getCareStart());
        assertEquals(createCareDTO.careEnd(), care.getCareEnd());
        assertEquals(createCareDTO.description(), care.getDescription());
        assertEquals(new BigDecimal("10.00"), care.getDailyPrice());
        assertEquals(createCareDTO.animalType(), care.getAnimal().getAnimalType());
        assertEquals(caretaker.getEmail(), care.getCaretaker().getEmail());
        assertEquals(client.getEmail(), care.getClient().getEmail());

    }

    @ParameterizedTest
    @MethodSource("parametrizedForMakeReservation")
    void makeReservation_ShouldThrowIllegalActionException(
            CreateCareDTO createCare,
            String userEmail,
            String caretakerEmail,
            Class<? extends Throwable> expectedExceptionClass
    ) {

        // When Then
       assertThrows(expectedExceptionClass,
               () -> careService.makeReservation(createCare, userEmail, caretakerEmail, ZoneId.systemDefault()));

    }

    static Stream<Arguments> parametrizedForMakeReservation() {
        String clientEmail = "clientEmail";
        String caretakerEmail = "caretakerEmail";

        return Stream.of(
                Arguments.of(
                        CreateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(1))
                                .careEnd(LocalDate.now().plusDays(5))
                                .description("Description")
                                .dailyPrice(new BigDecimal("10.00"))
                                .animalType("DOG")
                                .animalAttributeIds(new ArrayList<>())
                                .build(),
                        "wrongEmail",
                        caretakerEmail,
                        InvalidRoleException.class
                ),
                Arguments.of(
                        CreateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(1))
                                .careEnd(LocalDate.now().plusDays(5))
                                .description("Description")
                                .dailyPrice(new BigDecimal("10.00"))
                                .animalType("DOG")
                                .animalAttributeIds(new ArrayList<>())
                                .build(),
                        caretakerEmail,
                        caretakerEmail,
                        InvalidRoleException.class
                ),
                Arguments.of(
                        CreateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(7))
                                .careEnd(LocalDate.now().plusDays(5))
                                .description("Description")
                                .dailyPrice(new BigDecimal("10.00"))
                                .animalType("DOG")
                                .animalAttributeIds(new ArrayList<>())
                                .build(),
                        clientEmail,
                        clientEmail,
                        InvalidRoleException.class
                )
        );
    }

    @Test
    void updateCare_ShouldReturnProperUpdatedCare() {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.ACCEPTED);
        care.setCaretakerStatus(CareStatus.PENDING);

        UpdateCareDTO updateCareDTO = createUpdateCareDTO(3, 9, "20.00");

        // When
        CareDTO result = careService.updateCare(care.getId(), updateCareDTO, caretaker.getEmail(), ZoneId.systemDefault());

        // Then
        Care updatedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(updatedCare);
        assertEquals(new BigDecimal("20.00"), updatedCare.getDailyPrice());
        assertEquals(LocalDate.now().plusDays(3), updatedCare.getCareStart());
        assertEquals(LocalDate.now().plusDays(9), updatedCare.getCareEnd());
        assertEquals(CareStatus.ACCEPTED, updatedCare.getCaretakerStatus());
        assertEquals(CareStatus.PENDING, updatedCare.getClientStatus());
    }

    @ParameterizedTest
    @MethodSource("parametrizedForUpdateCare")
    void updateCare_ShouldThrowIllegalActionException(UpdateCareDTO updateCare, String userEmail,
                                                      Class expectedExceptionClass) {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(expectedExceptionClass, () -> careService.updateCare(care.getId(), updateCare, userEmail, ZoneId.systemDefault()));

    }

    static Stream<Arguments> parametrizedForUpdateCare() {
        return Stream.of(
                Arguments.of(
                        createUpdateCareDTO(1, 5, "10.00"),
                        "wrongEmail",
                        IllegalActionException.class
                ),
                Arguments.of(
                        createUpdateCareDTO(1, 5, "10.00"),
                        "clientEmail",
                        IllegalActionException.class
                )
        );
    }

    @Test
    void updateCare_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        UpdateCareDTO updateCare = createUpdateCareDTO(1, 5, "10.00");

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.updateCare(care.getId(), updateCare, caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void updateCare_WhenCaretakerStatusIsAccepted_ShouldThrowStateTransitionException() {

        // Given
        UpdateCareDTO updateCare = createUpdateCareDTO(1, 5, "10.00");

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.updateCare(care.getId(), updateCare, caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByCaretaker_ShouldReturnProperAcceptedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When
        CareDTO result = careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED);

        // Then
        Care acceptedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(acceptedCare);
        assertEquals(CareStatus.AWAITING_PAYMENT, acceptedCare.getCaretakerStatus());
        assertEquals(CareStatus.AWAITING_PAYMENT, acceptedCare.getClientStatus());

    }

    @Test
    void acceptCareByCaretaker_WhenLoggedUserIsClient_ShouldThrowIllegalInvalidRoleException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(InvalidRoleException.class,
                () -> careService.caretakerChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void acceptCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void acceptCareByCaretaker_WhenCaretakerStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void acceptCareByCaretaker_WhenClientStatusIsPending_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void acceptCareByClient_ShouldReturnProperAcceptedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);

        // When
        CareDTO result = careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED);

        // Then
        Care acceptedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(acceptedCare);
        assertEquals(CareStatus.ACCEPTED, acceptedCare.getClientStatus());

    }

    @Test
    void acceptCareByClient_WhenLoggedUserIsCaretaker_ShouldThrowInvalidRoleException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(InvalidRoleException.class,
                () -> careService.clientChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void acceptCareByClient_WhenCareIsTerminated_ShouldThrowInvalidRoleException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void acceptCareByClient_WhenClientStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED));

    }

    @Test
    void rejectCareByCaretaker_ShouldReturnProperRejectedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When
        CareDTO result = careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED);

        // Then
        Care rejectedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(rejectedCare);
        assertEquals(CareStatus.CANCELLED, rejectedCare.getCaretakerStatus());

    }

    @Test
    void rejectCareByCaretaker_WhenLoggedUserIsClient_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(InvalidRoleException.class,
                () -> careService.caretakerChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED));

    }

    @Test
    void rejectCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.OUTDATED);
        careRepository.saveAndFlush(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED));

    }

    @Test
    void cancelCareByClient_ShouldReturnProperCancelledCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When
        CareDTO result = careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED);

        // Then
        Care cancelledCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(cancelledCare);
        assertEquals(CareStatus.CANCELLED, cancelledCare.getClientStatus());

    }

    @Test
    void cancelCareByClient_WhenLoggedUserIsCaretaker_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(InvalidRoleException.class,
                () -> careService.clientChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED));

    }

    @Test
    void cancelCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(StateTransitionException.class,
                () -> careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED));

    }

    @ParameterizedTest
    @MethodSource("parameterProviderForGetCaretakerCares")
    @Transactional(readOnly = true)
    void getCaretakerCares_ShouldReturnProperCaretakerCares(CareSearchCriteria filters, Role selectedProfile,
                                                            Set<String> emails, String userEmail, int expectedSize) {

        // Given
        Client secondClient = PersistenceUtils.addClient(appUserRepository, clientRepository,
                createMockClient("second", "second", "secondClientEmail"));

        Caretaker secondCaretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository,
                createMockCaretaker("secondCaretakerEmail"));

        List<Care> cares = createCares(secondClient,secondCaretaker);

        // When
        Page<CareDTO> result = careService.getCares(Pageable.ofSize(10), filters, emails, userEmail,
                selectedProfile, ZoneId.systemDefault());

        // Then
        assertEquals(expectedSize, result.getTotalElements());
    }

    @Test
    void getCaretakerCares_sortingParamsShouldAlignWithDTO() {
        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(Care.class);
        fieldNames.addAll(List.of("animal_animalType", "caretaker_email", "client_email"));

        for(String fieldName: fieldNames) {
            assertDoesNotThrow(() -> careService.getCares(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    CareSearchCriteria.builder().build(),
                    Set.of(),
                    caretaker.getEmail(),
                    Role.CARETAKER,
                    ZoneId.systemDefault()
            ));
        }
    }

    private static UpdateCareDTO createUpdateCareDTO(int daysToAdd, int daysToAdd1, String val) {
        return UpdateCareDTO.builder()
                .careStart(LocalDate.now().plusDays(daysToAdd))
                .careEnd(LocalDate.now().plusDays(daysToAdd1))
                .dailyPrice(new BigDecimal(val))
                .build();
    }

    private static Stream<Arguments> parameterProviderForGetCaretakerCares() {
        return Stream.of(
                Arguments.of(
                        CareSearchCriteria.builder()
                                .animalTypes(Set.of("DOG"))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        3
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minCreatedTime(ZonedDateTime.of(2024, 5, 1, 0, 0, 0, 0, ZoneId.systemDefault()))
                                .maxCreatedTime(ZonedDateTime.of(2024, 5, 31, 23, 59, 59, 0, ZoneId.systemDefault()))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minCareStart(LocalDate.of(2024, 5, 1))
                                .maxCareStart(LocalDate.of(2024, 7, 10))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        3
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minCareEnd(LocalDate.of(2024, 6, 1))
                                .maxCareEnd(LocalDate.of(2024, 8, 10))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        2
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minDailyPrice(new BigDecimal("15.00"))
                                .maxDailyPrice(new BigDecimal("20.00"))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        2
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        5
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .build(),
                        Role.CARETAKER,
                        Set.of("clientEmail"),
                        "caretakerEmail",
                        3
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .build(),
                        Role.CARETAKER,
                        Set.of("secondClientEmail"),
                        "caretakerEmail",
                        2
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .build(),
                        Role.CARETAKER,
                        Set.of("wrongEmail"),
                        "caretakerEmail",
                        0
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .caretakerStatuses(Set.of(CareStatus.PENDING))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        3
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .caretakerStatuses(Set.of(CareStatus.AWAITING_PAYMENT))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .clientStatuses(Set.of(CareStatus.AWAITING_PAYMENT))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .clientStatuses(Set.of(CareStatus.ACCEPTED))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        2
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .clientStatuses(Set.of(CareStatus.CANCELLED))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minCareStart(LocalDate.of(2024, 6, 10))
                                .maxCareStart(LocalDate.of(2024, 6, 15))
                                .build(),
                        Role.CARETAKER,
                        Set.of("clientEmail"),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minCreatedTime(ZonedDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneId.systemDefault()))
                                .maxCreatedTime(ZonedDateTime.of(2024, 6, 29, 0, 0, 0, 0, ZoneId.systemDefault()))
                                .build(),
                        Role.CARETAKER,
                        Set.of("secondClientEmail"),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .animalTypes(Set.of("CAT"))
                                .build(),
                        Role.CARETAKER,
                        Set.of("secondClientEmail"),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .animalTypes(Set.of("CAT"))
                                .minCareStart(LocalDate.of(2024, 6, 20))
                                .maxCareStart(LocalDate.of(2024, 6, 25))
                                .build(),
                        Role.CARETAKER,
                        Set.of("secondClientEmail"),
                        "caretakerEmail",
                        0
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .build(),
                        Role.CLIENT,
                        Set.of(),
                        "secondClientEmail",
                        3
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .build(),
                        Role.CLIENT,
                        Set.of("secondCaretakerEmail"),
                        "secondClientEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minDailyPrice(new BigDecimal("20.00"))
                                .maxDailyPrice(new BigDecimal("70.00"))
                                .build(),
                        Role.CLIENT,
                        Set.of("caretakerEmail"),
                        "secondClientEmail",
                        2
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .minDailyPrice(new BigDecimal("20.00"))
                                .maxDailyPrice(new BigDecimal("70.00"))
                                .build(),
                        Role.CLIENT,
                        Set.of("caretakerEmail", "secondCaretakerEmail"),
                        "secondClientEmail",
                        3
                )
        );
    }

    private List<Care> createCares(Client secondClient, Caretaker secondCaretaker) {

        return List.of(
                PersistenceUtils.addCare(
                        careRepository,
                        caretaker,
                        client,
                        animalRepository.findById("DOG").orElseThrow(),
                        ZonedDateTime.of(2024, 5, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                        LocalDate.of(2024, 5, 15),
                        LocalDate.of(2024, 5, 20),
                        new BigDecimal("10.00"),
                        CareStatus.AWAITING_PAYMENT,
                        CareStatus.AWAITING_PAYMENT
                ),
                PersistenceUtils.addCare(
                        careRepository,
                        caretaker,
                        client,
                        animalRepository.findById("DOG").orElseThrow(),
                        ZonedDateTime.of(2024, 6, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                        LocalDate.of(2024, 6, 15),
                        LocalDate.of(2024, 6, 20),
                        new BigDecimal("15.00"),
                        CareStatus.PENDING,
                        CareStatus.ACCEPTED
                ),
                PersistenceUtils.addCare(
                        careRepository,
                        caretaker,
                        client,
                        animalRepository.findById("CAT").orElseThrow(),
                        ZonedDateTime.of(2024, 8, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                        LocalDate.of(2024, 8, 15),
                        LocalDate.of(2024, 8, 20),
                        new BigDecimal("50.00"),
                        CareStatus.PENDING,
                        CareStatus.PENDING
                ),
                PersistenceUtils.addCare(
                        careRepository,
                        caretaker,
                        secondClient,
                        animalRepository.findById("DOG").orElseThrow(),
                        ZonedDateTime.of(2024, 4, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                        LocalDate.of(2024, 4, 15),
                        LocalDate.of(2024, 4, 20),
                        new BigDecimal("70.00"),
                        CareStatus.PENDING,
                        CareStatus.CANCELLED
                ),
                PersistenceUtils.addCare(
                        careRepository,
                        caretaker,
                        secondClient,
                        animalRepository.findById("CAT").orElseThrow(),
                        ZonedDateTime.of(2024, 6, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                        LocalDate.of(2024, 6, 15),
                        LocalDate.of(2024, 6, 20),
                        new BigDecimal("20.00"),
                        CareStatus.CANCELLED,
                        CareStatus.ACCEPTED
                ),
                PersistenceUtils.addCare(
                        careRepository,
                        secondCaretaker,
                        secondClient,
                        animalRepository.findById("HORSE").orElseThrow(),
                        ZonedDateTime.of(2024, 6, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                        LocalDate.of(2024, 6, 15),
                        LocalDate.of(2024, 6, 20),
                        new BigDecimal("35.00"),
                        CareStatus.CANCELLED,
                        CareStatus.ACCEPTED
                )
        );
    }
}

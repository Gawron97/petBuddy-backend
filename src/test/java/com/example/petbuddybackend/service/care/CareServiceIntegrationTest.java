package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareDTO;
import com.example.petbuddybackend.dto.care.DetailedCareWithHistoryDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.CareStatisticsSearchCriteria;
import com.example.petbuddybackend.dto.statistic.MonthlyRevenueDTO;
import com.example.petbuddybackend.dto.user.SimplifiedAccountDataDTO;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.notification.NotificationService;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import com.example.petbuddybackend.utils.exception.throweable.user.InvalidRoleException;
import com.example.petbuddybackend.utils.exception.throweable.general.StateTransitionException;
import com.example.petbuddybackend.utils.exception.throweable.general.ForbiddenException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockClient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

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

    @Autowired
    private TransactionTemplate transactionTemplate;

    @SpyBean
    private NotificationService notificationService;

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
                .build();

        // When
        DetailedCareWithHistoryDTO result = transactionTemplate.execute(status ->
                careService.makeReservation(createCareDTO, client.getEmail(), caretaker.getEmail(), ZoneId.systemDefault()));


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
        Care careAfterTransaction = transactionTemplate.execute(status -> {
            Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
            care.setClientStatus(CareStatus.ACCEPTED);
            care.setCaretakerStatus(CareStatus.PENDING);
            return care;
        });

        UpdateCareDTO updateCareDTO = createUpdateCareDTO("20.00");

        // When
        DetailedCareWithHistoryDTO result = transactionTemplate.execute(status ->
                careService.updateCare(careAfterTransaction.getId(), updateCareDTO, caretaker.getEmail(), ZoneId.systemDefault())
        );

        // Then
        Care updatedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(updatedCare);
        assertEquals(new BigDecimal("20.00"), updatedCare.getDailyPrice()); // only price should change
        assertEquals(LocalDate.now().plusDays(2), updatedCare.getCareStart());
        assertEquals(LocalDate.now().plusDays(7), updatedCare.getCareEnd());
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
        transactionTemplate.execute(status ->
                assertThrows(expectedExceptionClass, () -> careService.updateCare(care.getId(), updateCare, userEmail, ZoneId.systemDefault())));

    }

    static Stream<Arguments> parametrizedForUpdateCare() {
        return Stream.of(
                Arguments.of(
                        createUpdateCareDTO("10.00"),
                        "wrongEmail",
                        IllegalActionException.class
                ),
                Arguments.of(
                        createUpdateCareDTO("10.00"),
                        "clientEmail",
                        IllegalActionException.class
                )
        );
    }

    @Test
    void updateCare_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        UpdateCareDTO updateCare = createUpdateCareDTO("10.00");

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.updateCare(care.getId(), updateCare, caretaker.getEmail(), ZoneId.systemDefault())));


    }

    @Test
    void updateCare_WhenCaretakerStatusIsAccepted_ShouldThrowStateTransitionException() {

        // Given
        UpdateCareDTO updateCare = createUpdateCareDTO("10.00");

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.updateCare(care.getId(), updateCare, caretaker.getEmail(), ZoneId.systemDefault())));

    }

    @Test
    void acceptCareByCaretaker_ShouldReturnProperAcceptedCare() {

        // Given
        Care care = transactionTemplate.execute(status ->
                PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );

        // When
        DetailedCareWithHistoryDTO result = transactionTemplate.execute(status ->
                careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)
        );

        // Then
        Care acceptedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(acceptedCare);
        assertEquals(CareStatus.READY_TO_PROCEED, acceptedCare.getCaretakerStatus());
        assertEquals(CareStatus.READY_TO_PROCEED, acceptedCare.getClientStatus());

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
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)));

    }

    @Test
    void acceptCareByCaretaker_WhenCaretakerStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)));


    }

    @Test
    void acceptCareByCaretaker_WhenClientStatusIsPending_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);

        // When Then
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)));

    }

    @Test
    void acceptCareByClient_ShouldReturnProperAcceptedCare() {

        // Given
        Care careAfterTransaction = transactionTemplate.execute(status -> {
            Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
            care.setClientStatus(CareStatus.PENDING);
                    return careRepository.save(care);
        });


        // When
        DetailedCareWithHistoryDTO result = transactionTemplate.execute(status ->
                careService.clientChangeCareStatus(careAfterTransaction.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)
        );

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
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)));


    }

    @Test
    void acceptCareByClient_WhenClientStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.ACCEPTED)));


    }

    @Test
    void rejectCareByCaretaker_ShouldReturnProperRejectedCare() {

        // Given
        Care care = transactionTemplate.execute(status ->
                PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );

        // When
        DetailedCareWithHistoryDTO result = transactionTemplate.execute(status ->
                careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED)
        );

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
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.caretakerChangeCareStatus(care.getId(), caretaker.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED)));

    }

    @Test
    void cancelCareByClient_ShouldReturnProperCancelledCare() {

        // Given
        Care care = transactionTemplate.execute(status ->
                PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );

        // When
        DetailedCareWithHistoryDTO result = transactionTemplate.execute(status ->
                careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED)
        );

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
        transactionTemplate.execute(status ->
                assertThrows(StateTransitionException.class,
                        () -> careService.clientChangeCareStatus(care.getId(), client.getEmail(), ZoneId.systemDefault(), CareStatus.CANCELLED)));


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
        Page<DetailedCareDTO> result = careService.getCares(Pageable.ofSize(10), filters, emails, userEmail,
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

    private static UpdateCareDTO createUpdateCareDTO(String val) {
        return UpdateCareDTO.builder()
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
                                .caretakerStatuses(Set.of(CareStatus.READY_TO_PROCEED))
                                .build(),
                        Role.CARETAKER,
                        Set.of(),
                        "caretakerEmail",
                        1
                ),
                Arguments.of(
                        CareSearchCriteria.builder()
                                .clientStatuses(Set.of(CareStatus.READY_TO_PROCEED))
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
                        CareStatus.READY_TO_PROCEED,
                        CareStatus.READY_TO_PROCEED
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

    @Test
    void getUsersRelatedToYourCares_whenEvaluatedByCaretaker_shouldReturnProperAnswer() {

        //Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        //When
        Page<SimplifiedAccountDataDTO> usersRelatedToYourCares =
                careService.getUsersRelatedToYourCares(caretaker.getEmail(), Pageable.ofSize(10), Role.CARETAKER);

        //Then
        assertEquals(1, usersRelatedToYourCares.getTotalElements());
        assertEquals(client.getEmail(), usersRelatedToYourCares.getContent().get(0).email());

    }

    @Test
    void getUsersRelatedToYourCares_whenEvaluatedByClient_shouldReturnProperAnswer() {

        //Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        //When
        Page<SimplifiedAccountDataDTO> usersRelatedToYourCares =
                careService.getUsersRelatedToYourCares(client.getEmail(), Pageable.ofSize(10), Role.CLIENT);

        //Then
        assertEquals(1, usersRelatedToYourCares.getTotalElements());
        assertEquals(caretaker.getEmail(), usersRelatedToYourCares.getContent().get(0).email());

    }

    @Test
    void getUsersRelatedToYourCares_whenUserWithProvidedRoleNotExist_shouldReturnEmptyPage() {

        //Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        //When
        Page<SimplifiedAccountDataDTO> usersRelatedToYourCares =
                careService.getUsersRelatedToYourCares("wrong@email", Pageable.ofSize(10), Role.CLIENT);

        //Then
        assertEquals(0, usersRelatedToYourCares.getTotalElements());

    }

    @Test
    void getUsersRelatedToYourCares_whenUserDoesNotHaveCares_shouldReturnEmptyPage() {

        //When
        Page<SimplifiedAccountDataDTO> usersRelatedToYourCares =
                careService.getUsersRelatedToYourCares(client.getEmail(), Pageable.ofSize(10), Role.CLIENT);

        //Then
        assertEquals(0, usersRelatedToYourCares.getTotalElements());

    }


    @Test
    @Transactional
    void getCare_shouldReturnProperAnswer() {

        //Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        //When
        DetailedCareWithHistoryDTO result = careService.getCare(care.getId(), ZoneId.systemDefault(), client.getEmail());

        //Then
        assertEquals(care.getId(), result.id());
        assertEquals(care.getCaretakerStatus(), result.caretakerStatus());
        assertEquals(care.getClientStatus(), result.clientStatus());
        assertEquals(care.getCareStart(), result.careStart());

    }

    @Test
    void getCare_whenCareNotExists_shouldThrowNotFoundException() {

        //When Then
        assertThrows(NotFoundException.class,
                () -> careService.getCare(1L, ZoneId.systemDefault(), client.getEmail()));

    }

    @Test
    void getCare_whenUserNotParticipateInCare_shouldThrowForbiddenException() {

        //Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        //When Then
        transactionTemplate.execute(status ->
                assertThrows(ForbiddenException.class,
                        () -> careService.getCare(care.getId(), ZoneId.systemDefault(), "wrong@email")));

    }

    @Test
    void testSendNotificationForConfirmCares_shouldTriggerSendingAndPersistingNotification() {

        //Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCareStart(LocalDate.now());
        careRepository.save(care);

        Care care2 = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("CAT").orElseThrow());
        care2.setCareStart(LocalDate.now());
        careRepository.save(care2);

        //When
        careService.sendNotificationForConfirmCares();

        //Then
        verify(notificationService, times(2))
                .addNotificationForCaretakerAndSend(anyLong(), any(), any(), anyString(), any());

    }

    @ParameterizedTest
    @MethodSource("parameterProviderForGetMonthlyRevenue")
    void testGetMonthlyRevenue(CareStatisticsSearchCriteria filters, BigDecimal expectedRevenue) {

        //Given
        Care care = PersistenceUtils.addCare(
                careRepository,
                caretaker,
                client,
                animalRepository.findById("DOG").orElseThrow(),
                ZonedDateTime.of(2024, 5, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                LocalDate.of(2024, 5, 15),
                LocalDate.of(2024, 5, 20),
                new BigDecimal("10.01"),
                CareStatus.CONFIRMED,
                CareStatus.CONFIRMED
        );

        Care care2 = PersistenceUtils.addCare(
                careRepository,
                caretaker,
                client,
                animalRepository.findById("CAT").orElseThrow(),
                ZonedDateTime.of(2023, 5, 10, 12, 0, 0, 0, ZoneId.systemDefault()),
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 1, 20),
                new BigDecimal("40.01"),
                CareStatus.CONFIRMED,
                CareStatus.CONFIRMED
        );

        //When
        MonthlyRevenueDTO result = careService.getMonthlyRevenue(
                caretaker.getEmail(),
                filters,
                Set.of()
        );

        //Then
        assertEquals(expectedRevenue, result.monthlyRevenue().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));

    }

    private static Stream<Arguments> parameterProviderForGetMonthlyRevenue() {
        return Stream.of(
                Arguments.of(
                        CareStatisticsSearchCriteria.builder()
                                .build(),
                        BigDecimal.valueOf(300.12)
                ),
                Arguments.of(
                        CareStatisticsSearchCriteria.builder()
                                .minCareStart(YearMonth.of(2024, 5))
                                .maxCareStart(YearMonth.of(2024, 5))
                                .build(),
                        BigDecimal.valueOf(60.06)
                ),
                Arguments.of(
                        CareStatisticsSearchCriteria.builder()
                                .animalTypes(Set.of("CAT"))
                                .build(),
                        BigDecimal.valueOf(240.06)
                )
        );
    }

}

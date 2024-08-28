package com.example.petbuddybackend.service.care;

import com.example.petbuddybackend.config.TestDataConfiguration;
import com.example.petbuddybackend.dto.care.CareDTO;
import com.example.petbuddybackend.dto.care.CreateCareDTO;
import com.example.petbuddybackend.dto.care.UpdateCareDTO;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.CareRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.stream.Stream;

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
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .build();

        // When
        CareDTO result = careService.makeReservation(createCareDTO, client.getEmail(), ZoneId.systemDefault());

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
    void makeReservation_ShouldThrowIllegalActionException(CreateCareDTO createCare, String userEmail,
                                                           Class expectedExceptionClass) {

        // When Then
       assertThrows(expectedExceptionClass, () -> careService.makeReservation(createCare, userEmail, ZoneId.systemDefault()));

    }

    static Stream<Arguments> parametrizedForMakeReservation() {
        return Stream.of(
                Arguments.of(
                        CreateCareDTO.builder()
                        .careStart(LocalDate.now().plusDays(1))
                        .careEnd(LocalDate.now().plusDays(5))
                        .description("Description")
                        .dailyPrice(new BigDecimal("10.00"))
                        .animalType("DOG")
                        .animalAttributeIds(new ArrayList<>())
                        .caretakerEmail("email")
                        .clientEmail("clientEmail")
                        .build(),
                        "wrongEmail",
                        IllegalActionException.class
                        ),
                Arguments.of(
                        CreateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(1))
                                .careEnd(LocalDate.now().plusDays(5))
                                .description("Description")
                                .dailyPrice(new BigDecimal("10.00"))
                                .animalType("DOG")
                                .animalAttributeIds(new ArrayList<>())
                                .caretakerEmail("email")
                                .clientEmail("clientEmail")
                                .build(),
                        "email",
                        IllegalActionException.class
                ),
                Arguments.of(
                        CreateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(7))
                                .careEnd(LocalDate.now().plusDays(5))
                                .description("Description")
                                .dailyPrice(new BigDecimal("10.00"))
                                .animalType("DOG")
                                .animalAttributeIds(new ArrayList<>())
                                .caretakerEmail("email")
                                .clientEmail("clientEmail")
                                .build(),
                        "clientEmail",
                        IllegalActionException.class
                )

        );
    }

    @Test
    void updateCare_ShouldReturnProperUpdatedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        UpdateCareDTO updateCareDTO = UpdateCareDTO.builder()
                .careStart(LocalDate.now().plusDays(3))
                .careEnd(LocalDate.now().plusDays(9))
                .dailyPrice(new BigDecimal("20.00"))
                .build();

        // When
        CareDTO result = careService.updateCare(care.getId(), updateCareDTO, caretaker.getEmail(), ZoneId.systemDefault());

        // Then
        Care updatedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(updatedCare);
        assertEquals(new BigDecimal("20.00"), updatedCare.getDailyPrice());
        assertEquals(LocalDate.now().plusDays(3), updatedCare.getCareStart());
        assertEquals(LocalDate.now().plusDays(9), updatedCare.getCareEnd());

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
                        UpdateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(1))
                                .careEnd(LocalDate.now().plusDays(5))
                                .dailyPrice(new BigDecimal("10.00"))
                                .build(),
                        "wrongEmail",
                        IllegalActionException.class
                ),
                Arguments.of(
                        UpdateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(1))
                                .careEnd(LocalDate.now().plusDays(5))
                                .dailyPrice(new BigDecimal("10.00"))
                                .build(),
                        "clientEmail",
                        IllegalActionException.class
                ),
                Arguments.of(
                        UpdateCareDTO.builder()
                                .careStart(LocalDate.now().plusDays(7))
                                .careEnd(LocalDate.now().plusDays(5))
                                .dailyPrice(new BigDecimal("10.00"))
                                .build(),
                        "caretakerEmail",
                        IllegalActionException.class
                )

        );
    }

    @Test
    void updateCare_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        UpdateCareDTO updateCare = UpdateCareDTO.builder()
                .careStart(LocalDate.now().plusDays(1))
                .careEnd(LocalDate.now().plusDays(5))
                .dailyPrice(new BigDecimal("10.00"))
                .build();

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.updateCare(care.getId(), updateCare, caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void updateCare_WhenCaretakerStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        UpdateCareDTO updateCare = UpdateCareDTO.builder()
                .careStart(LocalDate.now().plusDays(1))
                .careEnd(LocalDate.now().plusDays(5))
                .dailyPrice(new BigDecimal("10.00"))
                .build();

        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.updateCare(care.getId(), updateCare, caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByCaretaker_ShouldReturnProperAcceptedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When
        CareDTO result = careService.acceptCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault());

        // Then
        Care acceptedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(acceptedCare);
        assertEquals(CareStatus.AWAITING_PAYMENT, acceptedCare.getCaretakerStatus());
        assertEquals(CareStatus.AWAITING_PAYMENT, acceptedCare.getClientStatus());

    }

    @Test
    void acceptCareByCaretaker_WhenLoggedUserIsClient_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByCaretaker(care.getId(), client.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByCaretaker_WhenCaretakerStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByCaretaker_WhenClientStatusIsPending_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByClient_ShouldReturnProperAcceptedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);

        // When
        CareDTO result = careService.acceptCareByClient(care.getId(), client.getEmail(), ZoneId.systemDefault());

        // Then
        Care acceptedCare = careRepository.findById(result.id()).orElseThrow();
        assertNotNull(acceptedCare);
        assertEquals(CareStatus.ACCEPTED, acceptedCare.getClientStatus());

    }

    @Test
    void acceptCareByClient_WhenLoggedUserIsCaretaker_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByClient(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByClient(care.getId(), client.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void acceptCareByClient_WhenClientStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.acceptCareByClient(care.getId(), client.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void rejectCareByCaretaker_ShouldReturnProperRejectedCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When
        CareDTO result = careService.rejectCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault());

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
        assertThrows(IllegalActionException.class,
                () -> careService.rejectCareByCaretaker(care.getId(), client.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void rejectCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.rejectCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void rejectCareByCaretaker_WhenCaretakerStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.rejectCareByCaretaker(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void cancelCareByClient_ShouldReturnProperCancelledCare() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());

        // When
        CareDTO result = careService.cancelCareByClient(care.getId(), client.getEmail(), ZoneId.systemDefault());

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
        assertThrows(IllegalActionException.class,
                () -> careService.cancelCareByClient(care.getId(), caretaker.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void cancelCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setClientStatus(CareStatus.OUTDATED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.cancelCareByClient(care.getId(), client.getEmail(), ZoneId.systemDefault()));

    }

    @Test
    void cancelCareByClient_WhenCaretakerStatusIsAccepted_ShouldThrowIllegalActionException() {

        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").orElseThrow());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);

        // When Then
        assertThrows(IllegalActionException.class,
                () -> careService.cancelCareByClient(care.getId(), client.getEmail(), ZoneId.systemDefault()));

    }

}

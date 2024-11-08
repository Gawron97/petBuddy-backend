package com.example.petbuddybackend.controller;

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
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CareControllerIntegrationTest {

    public static final String CLIENT_EMAIL = "clientEmail";
    public static final String CARETAKER_EMAIL = "caretakerEmail";
    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Autowired
    private MockMvc mockMvc;

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

    private static final String CREATE_CARE_BODY = """
            {
                "careStart": "%s",
                "careEnd": "%s",
                "description": "%s",
                "dailyPrice": %s,
                "animalType": "%s",
                "animalAttributeIds": [%s],
                "caretakerEmail": "%s",
                "clientEmail": "%s"
            }
            """;

    private static final String UPDATE_CARE_BODY = """
            {
                "dailyPrice": %s
            }
            """;

    private Caretaker caretaker;
    private Client client;

    @BeforeEach
    void setUp() {
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
    }

    @AfterEach
    void tearDown() {
        careRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void makeReservation_ShouldReturnCreatedCare() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/{caretakerEmail}", CARETAKER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(2),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                CARETAKER_EMAIL,
                                CLIENT_EMAIL))
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.PENDING.name()))
                .andExpect(jsonPath("$.careStart").value(LocalDate.now().plusDays(2).toString()))
                .andExpect(jsonPath("$.careEnd").value(LocalDate.now().plusDays(7).toString()))
                .andExpect(jsonPath("$.description").value("Test care description"))
                .andExpect(jsonPath("$.dailyPrice").value(50.00))
                .andExpect(jsonPath("$.animalType").value("DOG"))
                .andExpect(jsonPath("$.caretakerEmail").value(CARETAKER_EMAIL))
                .andExpect(jsonPath("$.clientEmail").value(CLIENT_EMAIL));
    }

    @Test
    @WithMockUser(username = "anotherClient")
    void makeReservation_WhenClientEmailMismatch_ThenThrowIllegalActionException() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/{caretakerEmail}", CARETAKER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(2),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                CARETAKER_EMAIL,
                                CLIENT_EMAIL)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void makeReservation_WhenClientEmailEqualsCaretakerEmail_ThenThrowIllegalActionException() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/{caretakerEmail}", CARETAKER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(2),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                "email",
                                "email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void makeReservation_WhenEndDateIsAfterStartDate_ThenThrowIllegalActionException() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/{caretakerEmail}", CARETAKER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(8),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                CARETAKER_EMAIL,
                                CLIENT_EMAIL)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void updateCare_ShouldUpdateCareProperly() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/{careId}", careId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER)
                        .content(String.format(UPDATE_CARE_BODY,
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.PENDING.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.careStart").value(care.getCareStart().toString()))
                .andExpect(jsonPath("$.careEnd").value(care.getCareEnd().toString()))
                .andExpect(jsonPath("$.description").value("Test care description"))
                .andExpect(jsonPath("$.dailyPrice").value(60.00))
                .andExpect(jsonPath("$.animalType").value("DOG"))
                .andExpect(jsonPath("$.caretakerEmail").value(CARETAKER_EMAIL))
                .andExpect(jsonPath("$.clientEmail").value(CLIENT_EMAIL));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void updateCare_WhenClientTriesToEdit_ShouldThrowIllegalActionException() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/{careId}", careId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void updateCare_WhenCareIsCancelled_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/{careId}", careId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void updateCare_WhenCareIsAcceptedByCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/{careId}", careId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void updateCare_WhenEndDateIsBeforeStartDate_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/{careId}" , careId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void acceptCareByCaretaker_ShouldReturnAcceptedCare() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId)
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.AWAITING_PAYMENT.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.AWAITING_PAYMENT.name()));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void acceptCareByCaretaker_WhenUserIsNotCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void acceptCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.OUTDATED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void acceptCareByCaretaker_WhenCaretakerStatusIsNotPending_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.AWAITING_PAYMENT);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void acceptCareByCaretaker_WhenClientStatusIsNotAccepted_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void acceptCareByClient_ShouldReturnAcceptedCare() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.PENDING.name()));
    }

    @Test
    @WithMockUser(username = "email")
    void acceptCareByClient_WhenLoggedUserIsNotClient_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId)
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void acceptCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void acceptCareByClient_WhenClientStatusIsAlreadyAccepted_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/accept", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void rejectCareByCaretaker_ShouldReturnRejectedCare() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId)
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.CANCELLED.name()));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void rejectCareByCaretaker_WhenLoggedUserIsClient_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void rejectCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void rejectCareByCaretaker_WhenCaretakerStatusIsNotPending_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void cancelCareByClient_ShouldReturnCancelledCare() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.CANCELLED.name()));
    }

    @Test
    @WithMockUser(username = "email")
    void cancelCareByClient_WhenLoggedUserIsCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId)
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void cancelCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void cancelCareByClient_WhenCareIsAlreadyAcceptedByCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/{careId}/reject", careId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void getCares_whenRoleIsCaretaker_ShouldReturnProperCares() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        mockMvc.perform(get("/api/care")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].clientStatus").value(CareStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.content[0].caretakerStatus").value(CareStatus.PENDING.name()))
                .andExpect(jsonPath("$.content[0].careStart").value(LocalDate.now().plusDays(2).toString()))
                .andExpect(jsonPath("$.content[0].careEnd").value(LocalDate.now().plusDays(7).toString()))
                .andExpect(jsonPath("$.content[0].description").value("Test care description"))
                .andExpect(jsonPath("$.content[0].dailyPrice").value(50.00))
                .andExpect(jsonPath("$.content[0].animalType").value("DOG"))
                .andExpect(jsonPath("$.content[0].caretakerEmail").value(CARETAKER_EMAIL))
                .andExpect(jsonPath("$.content[0].clientEmail").value(CLIENT_EMAIL));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void getCaretakerCares_whenRoleIsClient_ShouldReturnProperCares() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        mockMvc.perform(get("/api/care")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].clientStatus").value(CareStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.content[0].caretakerStatus").value(CareStatus.PENDING.name()))
                .andExpect(jsonPath("$.content[0].careStart").value(LocalDate.now().plusDays(2).toString()))
                .andExpect(jsonPath("$.content[0].careEnd").value(LocalDate.now().plusDays(7).toString()))
                .andExpect(jsonPath("$.content[0].description").value("Test care description"))
                .andExpect(jsonPath("$.content[0].dailyPrice").value(50.00))
                .andExpect(jsonPath("$.content[0].animalType").value("DOG"))
                .andExpect(jsonPath("$.content[0].caretakerEmail").value(CARETAKER_EMAIL))
                .andExpect(jsonPath("$.content[0].clientEmail").value(CLIENT_EMAIL));
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void getCaretakerCares_whenRoleIsNotProvided_ShouldThrowBadRequest() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        mockMvc.perform(get("/api/care")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void getCaretakerCares_whenDatesInvalid_ShouldThrowDateRangeException() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        mockMvc.perform(get("/api/care")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CARETAKER)
                        .param("minCareStart", LocalDate.now().plusDays(8).toString())
                        .param("maxCareStart", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void getCare_ShouldReturnProperAnswer() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        mockMvc.perform(get("/api/care/{careId}", care.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


}

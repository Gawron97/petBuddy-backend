package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.config.TestDataConfiguration;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestDataConfiguration.class)
public class CareControllerIntegrationTest {

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
                "careStart": "%s",
                "careEnd": "%s",
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
    @WithMockUser(username = "clientEmail")
    void makeReservation_ShouldReturnCreatedCare() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(2),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                "email",
                                "clientEmail")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.ACCEPTED.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.PENDING.name()))
//                .andExpect(jsonPath("$.careStart").value(LocalDate.now().plusDays(2)))
//                .andExpect(jsonPath("$.careEnd").value(LocalDate.now().plusDays(7)))
                .andExpect(jsonPath("$.description").value("Test care description"))
                .andExpect(jsonPath("$.dailyPrice").value(50.00))
                .andExpect(jsonPath("$.animalType").value("DOG"))
                .andExpect(jsonPath("$.caretakerEmail").value("email"))
                .andExpect(jsonPath("$.clientEmail").value("clientEmail"));
    }

    @Test
    @WithMockUser(username = "anotherClient")
    void makeReservation_WhenClientEmailMismatch_ThenThrowIllegalActionException() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(2),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                "email",
                                "clientEmail")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void makeReservation_WhenClientEmailEqualsCaretakerEmail_ThenThrowIllegalActionException() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/reservation")
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
    @WithMockUser(username = "clientEmail")
    void makeReservation_WhenEndDateIsAfterStartDate_ThenThrowIllegalActionException() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/care/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARE_BODY,
                                LocalDate.now().plusDays(8),
                                LocalDate.now().plusDays(7),
                                "Test care description",
                                new BigDecimal("50.00"),
                                "DOG",
                                "",
                                "email",
                                "clientEmail")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void updateCare_ShouldUpdateCareProperly() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/" + careId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                LocalDate.now().plusDays(3),
                                LocalDate.now().plusDays(8),
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.PENDING.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.PENDING.name()))
//                .andExpect(jsonPath("$.careStart").value(LocalDate.now().plusDays(2)))
//                .andExpect(jsonPath("$.careEnd").value(LocalDate.now().plusDays(7)))
                .andExpect(jsonPath("$.description").value("Test care description"))
                .andExpect(jsonPath("$.dailyPrice").value(60.00))
                .andExpect(jsonPath("$.animalType").value("DOG"))
                .andExpect(jsonPath("$.caretakerEmail").value("email"))
                .andExpect(jsonPath("$.clientEmail").value("clientEmail"));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void updateCare_WhenClientTriesToEdit_ShouldThrowIllegalActionException() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/" + careId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                LocalDate.now().plusDays(3),
                                LocalDate.now().plusDays(8),
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void updateCare_WhenCareIsCancelled_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/" + careId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                LocalDate.now().plusDays(3),
                                LocalDate.now().plusDays(8),
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void updateCare_WhenCareIsAcceptedByCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/" + careId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                LocalDate.now().plusDays(3),
                                LocalDate.now().plusDays(8),
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void updateCare_WhenEndDateIsBeforeStartDate_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/care/" + careId + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARE_BODY,
                                LocalDate.now().plusDays(9),
                                LocalDate.now().plusDays(8),
                                new BigDecimal("60.00")))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void acceptCareByCaretaker_ShouldReturnAcceptedCare() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientStatus").value(CareStatus.AWAITING_PAYMENT.name()))
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.AWAITING_PAYMENT.name()));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void acceptCareByCaretaker_WhenUserIsNotCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void acceptCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.OUTDATED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void acceptCareByCaretaker_WhenCaretakerStatusIsNotPending_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.AWAITING_PAYMENT);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void acceptCareByCaretaker_WhenClientStatusIsNotAccepted_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void acceptCareByClient_ShouldReturnAcceptedCare() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.PENDING);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/client-accept"))
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
        mockMvc.perform(post("/api/care/" + careId + "/client-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void acceptCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/client-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void acceptCareByClient_WhenClientStatusIsAlreadyAccepted_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/client-accept"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void rejectCareByCaretaker_ShouldReturnRejectedCare() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caretakerStatus").value(CareStatus.CANCELLED.name()));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void rejectCareByCaretaker_WhenLoggedUserIsClient_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-reject"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void rejectCareByCaretaker_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setClientStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-reject"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "email")
    void rejectCareByCaretaker_WhenCaretakerStatusIsNotPending_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/caretaker-reject"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void cancelCareByClient_ShouldReturnCancelledCare() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/client-cancel"))
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
        mockMvc.perform(post("/api/care/" + careId + "/client-cancel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void cancelCareByClient_WhenCareIsTerminated_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.CANCELLED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/client-cancel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void cancelCareByClient_WhenCareIsAlreadyAcceptedByCaretaker_ShouldThrowIllegalActionException() throws Exception {
        // Given
        Care care = PersistenceUtils.addCare(careRepository, caretaker, client, animalRepository.findById("DOG").get());
        care.setCaretakerStatus(CareStatus.ACCEPTED);
        careRepository.save(care);
        // When and Then
        Long careId = careRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/care/" + careId + "/client-cancel"))
                .andExpect(status().isBadRequest());
    }


}

package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CaretakerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    private static final String CREATE_CARETAKER_BODY = """
            {
                "phoneNumber": "%s",
                "description": "%s",
                "address": {
                    "city": "%s",
                    "zipCode": "%s",
                    "voivodeship": "%s",
                    "street": "%s",
                    "buildingNumber": "%s",
                    "apartmentNumber": "%s"
                }
            }
            """;

    private static final String UPDATE_CARETAKER_BODY = """
            {
                "phoneNumber": "%s",
                "description": "%s",
                "address": {
                    "city": "%s",
                    "zipCode": "%s",
                    "voivodeship": "%s",
                    "street": "%s",
                    "buildingNumber": "%s",
                    "apartmentNumber": "%s"
                }
            }
            """;


    @BeforeEach
    void setUp() {
        PersistenceUtils.addAppUser(appUserRepository);
    }

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "email")
    void addCaretaker_ShouldReturnCreatedCaretaker() throws Exception {
        // When and Then
        mockMvc.perform(post("/api/caretaker/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARETAKER_BODY,
                                "123456789",
                                "Test description",
                                "City",
                                "00-000",
                                Voivodeship.MAZOWIECKIE.name(),
                                "Street",
                                "10",
                                "20")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.address.city").value("City"))
                .andExpect(jsonPath("$.address.zipCode").value("00-000"))
                .andExpect(jsonPath("$.address.voivodeship").value(Voivodeship.MAZOWIECKIE.name()))
                .andExpect(jsonPath("$.address.street").value("Street"))
                .andExpect(jsonPath("$.address.buildingNumber").value("10"))
                .andExpect(jsonPath("$.address.apartmentNumber").value("20"));
    }

    @Test
    @WithMockUser(username = "email")
    void addCaretaker_WhenAppUserNotExists_ShouldThrowNotFound() throws Exception {

        //Given
        appUserRepository.deleteAll();

        // When and Then
        mockMvc.perform(post("/api/caretaker/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARETAKER_BODY,
                                "123456789",
                                "Test description",
                                "City",
                                "00-000",
                                Voivodeship.MAZOWIECKIE.name(),
                                "Street",
                                "10",
                                "20")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "email")
    void editCaretaker_WhenCaretakerNotExists_ShouldThrowNotFound() throws Exception {
        // When and Then
        mockMvc.perform(patch("/api/caretaker/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARETAKER_BODY,
                                "987654321",
                                "Updated description",
                                "New City",
                                "11-111",
                                Voivodeship.PODLASKIE.name(),
                                "New Street",
                                "11",
                                "21")))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "email")
    void editCaretaker_WhenCaretakerExists_ShouldUpdateOnlyProvidedFields() throws Exception {
        // Given
        Caretaker caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);

        // When and Then
        mockMvc.perform(patch("/api/caretaker/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARETAKER_BODY,
                                "",
                                "Updated description",
                                "",
                                "11-111",
                                Voivodeship.PODLASKIE.name(),
                                "New Street",
                                "11",
                                "21")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(caretaker.getPhoneNumber()))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.address.city").value(caretaker.getAddress().getCity()))
                .andExpect(jsonPath("$.address.zipCode").value("11-111"))
                .andExpect(jsonPath("$.address.voivodeship").value(Voivodeship.PODLASKIE.name()))
                .andExpect(jsonPath("$.address.street").value("New Street"))
                .andExpect(jsonPath("$.address.buildingNumber").value("11"))
                .andExpect(jsonPath("$.address.apartmentNumber").value("21"));
    }

}

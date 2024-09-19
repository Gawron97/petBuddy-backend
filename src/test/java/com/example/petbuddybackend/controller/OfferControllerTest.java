package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.availability.AvailabilityRangeDTO;
import com.example.petbuddybackend.dto.offer.ModifyConfigurationDTO;
import com.example.petbuddybackend.dto.offer.ModifyOfferDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.service.offer.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OfferControllerTest {

    @MockBean
    private OfferService offerService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String CREATE_OFFERS_AVAILABILITY_BODY = """
        {
            "offerIds": [%s],
            "availabilityRanges": [
                {
                    "availableFrom": "%s",
                    "availableTo": "%s"
                },
                {
                    "availableFrom": "%s",
                    "availableTo": "%s"
                }
            ]
        }
        """;

    private static final String CREATE_OR_UPDATE_OFFER = """
        {
            "description": "%s",
            "animal": {
                "animalType": "%s"
            }
        }
        """;

    private static final String CREATE_OR_UPDATE_CONFIGURATION = """
        {
            "description": "%s",
            "dailyPrice": 10.0,
            "selectedOptions": {
                "SIZE": ["BIG"]
            }
        }
        """;

    @Test
    @WithMockUser
    void addOffer_ShouldReturnCreatedOffer() throws Exception {
        // Given
        OfferDTO offerDTO = OfferDTO.builder()
                .description("Test Offer")
                .offerConfigurations(
                        List.of(OfferConfigurationDTO.builder()
                                .description("Test Configuration")
                                .selectedOptions(Map.of("SIZE", List.of("BIG")))
                                .build()
                ))
                .build();
        when(offerService.addOrEditOffer(any(ModifyOfferDTO.class), anyString())).thenReturn(offerDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/add-or-edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_OR_UPDATE_OFFER,
                                "Test Offer",
                                "DOG")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer"))
                .andExpect(jsonPath("$.offerConfigurations[0].description").value("Test Configuration"))
                .andExpect(jsonPath("$.offerConfigurations[0].selectedOptions.SIZE[0]").value("BIG"));

        verify(offerService, times(1)).addOrEditOffer(any(ModifyOfferDTO.class), anyString());
    }

    @Test
    @WithMockUser
    void editConfiguration_ShouldReturnUpdatedConfiguration() throws Exception {
        // Given
        OfferConfigurationDTO configDTO = OfferConfigurationDTO.builder().description("Updated Configuration").build();
        when(offerService.editConfiguration(anyLong(), any(ModifyConfigurationDTO.class))).thenReturn(configDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/configuration/1/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_OR_UPDATE_CONFIGURATION, "Updated Configuration")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Configuration"));

        verify(offerService, times(1)).editConfiguration(anyLong(), any(ModifyConfigurationDTO.class));
    }

    @Test
    @WithMockUser
    void deleteConfiguration_ShouldReturnDeletedConfiguration() throws Exception {
        // Given
        OfferDTO offerDTO = OfferDTO.builder().description("Deleted Configuration").build();
        when(offerService.deleteConfiguration(anyLong())).thenReturn(offerDTO);

        // When and Then
        mockMvc.perform(delete("/api/caretaker/offer/configuration/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Deleted Configuration"));

        verify(offerService, times(1)).deleteConfiguration(anyLong());
    }

    @Test
    @WithMockUser
    void setAvailabilityForOffers_ShouldReturnOfferWithSetAvailabilities() throws Exception {

        // Given
        List<AvailabilityRangeDTO> availabilityRanges = List.of(
                AvailabilityRangeDTO.builder()
                        .availableFrom(ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()))
                        .availableTo(ZonedDateTime.of(2027, 1, 10, 0, 0, 0, 0, ZoneId.systemDefault()))
                        .build(),
                AvailabilityRangeDTO.builder()
                        .availableFrom(ZonedDateTime.of(2027, 1, 10, 0, 0, 0, 0, ZoneId.systemDefault()))
                        .availableTo(ZonedDateTime.of(2027, 1, 20, 0, 0, 0, 0, ZoneId.systemDefault()))
                        .build()
        );
        OfferDTO offerDTO = OfferDTO.builder()
                .availabilities(availabilityRanges)
                .build();
        when(offerService.setAvailabilityForOffers(any(), any())).thenReturn(List.of(offerDTO));

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/set-availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_OFFERS_AVAILABILITY_BODY,
                                1,
                                "2027-01-01 00:00:00.000 +0000",
                                "2027-01-10 00:00:00.000 +0000",
                                "2027-01-10 00:00:00.000 +0000",
                                "2027-01-20 00:00:00.000 +0000"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].availabilities").isArray())
                .andExpect(jsonPath("$[0].availabilities").exists());

    }

}

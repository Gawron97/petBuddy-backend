package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.availability.AvailabilityRangeDTO;
import com.example.petbuddybackend.dto.offer.ModifyConfigurationDTO;
import com.example.petbuddybackend.dto.offer.ModifyOfferDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.offer.OfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    @Value("${header-name.role}")
    private String roleHeaderName;

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

    private final static String ADD_CONFIGURATIONS_FOR_OFFER = """
        [
            {
                "description": "New Configuration",
                "dailyPrice": 20.0,
                "selectedOptions": {
                    "SIZE": ["SMALL"]
                }
            }
        ]
        """;

    private final static String ADD_AMENITIES_FOR_OFFER = """
        ["AMENITY1", "AMENITY2"]
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
                                "DOG"))
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer"))
                .andExpect(jsonPath("$.offerConfigurations[0].description").value("Test Configuration"))
                .andExpect(jsonPath("$.offerConfigurations[0].selectedOptions.SIZE[0]").value("BIG"));

        verify(offerService, times(1)).addOrEditOffer(any(ModifyOfferDTO.class), anyString());
    }

    @Test
    @WithMockUser
    void deleteOffer_ShouldReturnCreatedOffer() throws Exception {
        // Given
        OfferDTO offerDTO = OfferDTO.builder()
                .id(1L)
                .description("Test Offer")
                .offerConfigurations(
                        List.of(OfferConfigurationDTO.builder()
                                .description("Test Configuration")
                                .selectedOptions(Map.of("SIZE", List.of("BIG")))
                                .build()
                        ))
                .build();
        when(offerService.deleteOffer(anyLong(), anyString())).thenReturn(offerDTO);

        // When and Then
        mockMvc.perform(delete("/api/caretaker/offer/" + 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer"))
                .andExpect(jsonPath("$.offerConfigurations[0].description").value("Test Configuration"))
                .andExpect(jsonPath("$.offerConfigurations[0].selectedOptions.SIZE[0]").value("BIG"));

        verify(offerService, times(1)).deleteOffer(anyLong(), anyString());
    }

    @Test
    @WithMockUser
    void addConfigurationsForOffer_ShouldReturnUpdatedOffer() throws Exception {
        // Given
        OfferDTO offerDTO = OfferDTO.builder()
                .description("Test Offer with new configurations")
                .offerConfigurations(List.of(
                        OfferConfigurationDTO.builder()
                                .description("New Configuration")
                                .dailyPrice(BigDecimal.valueOf(20.0))
                                .selectedOptions(Map.of("SIZE", List.of("SMALL")))
                                .build()
                ))
                .build();

        when(offerService.addConfigurationsForOffer(anyLong(), anyList(), anyString())).thenReturn(offerDTO);


        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/1/configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_CONFIGURATIONS_FOR_OFFER)
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer with new configurations"))
                .andExpect(jsonPath("$.offerConfigurations[0].description").value("New Configuration"))
                .andExpect(jsonPath("$.offerConfigurations[0].dailyPrice").value(20.0))
                .andExpect(jsonPath("$.offerConfigurations[0].selectedOptions.SIZE[0]").value("SMALL"));

        verify(offerService, times(1)).addConfigurationsForOffer(anyLong(), anyList(), anyString());
    }

    @Test
    @WithMockUser
    void addAmenitiesForOffer_ShouldReturnUpdatedOfferWithNewAmenities() throws Exception {
        // Given
        OfferDTO offerDTO = OfferDTO.builder()
                .description("Test Offer with new amenities")
                .animalAmenities(List.of("toys", "garden"))
                .build();

        when(offerService.addAmenitiesForOffer(anyLong(), anySet(), anyString())).thenReturn(offerDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_AMENITIES_FOR_OFFER)
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer with new amenities"))
                .andExpect(jsonPath("$.animalAmenities[0]").value("toys"))
                .andExpect(jsonPath("$.animalAmenities[1]").value("garden"));

        verify(offerService, times(1)).addAmenitiesForOffer(anyLong(), anySet(), anyString());
    }

    @Test
    @WithMockUser
    void editConfiguration_ShouldReturnUpdatedConfiguration() throws Exception {
        // Given
        OfferConfigurationDTO configDTO = OfferConfigurationDTO.builder().description("Updated Configuration").build();
        when(offerService.editConfiguration(anyLong(), any(ModifyConfigurationDTO.class), any())).thenReturn(configDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/configuration/1/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_OR_UPDATE_CONFIGURATION, "Updated Configuration"))
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Configuration"));

        verify(offerService, times(1)).editConfiguration(anyLong(), any(ModifyConfigurationDTO.class), any());
    }

    @Test
    @WithMockUser
    void deleteConfiguration_ShouldReturnDeletedConfiguration() throws Exception {
        // Given
        OfferDTO offerDTO = OfferDTO.builder().description("Deleted Configuration").build();
        when(offerService.deleteConfiguration(anyLong(), any())).thenReturn(offerDTO);

        // When and Then
        mockMvc.perform(delete("/api/caretaker/offer/configuration/1/delete")
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Deleted Configuration"));

        verify(offerService, times(1)).deleteConfiguration(anyLong(), any());
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
                        ))
                        .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].availabilities").isArray())
                .andExpect(jsonPath("$[0].availabilities").exists());

    }

    @Test
    @WithMockUser
    void deleteAmenitiesFromOffer_ShouldReturnUpdatedOfferWithoutDeletedAmenities() throws Exception {

        //Given
        OfferDTO offer = OfferDTO.builder()
                        .description("Test Offer")
                        .animalAmenities(new ArrayList<>(List.of("AMENITY3")))
                        .build();

        when(offerService.deleteAmenitiesFromOffer(anyList(), any(), any())).thenReturn(offer);

        //When Then
        mockMvc.perform(post("/api/caretaker/offer/1/amenities-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"AMENITY1\", \"AMENITY2\"]")
                .header(roleHeaderName, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer"));

        verify(offerService, times(1)).deleteAmenitiesFromOffer(anyList(), any(), any());

    }

}

package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.service.offer.OfferService;
import com.example.petbuddybackend.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OfferControllerTest {

    @MockBean
    private OfferService offerService;

    @MockBean
    private UserService userService;

    @InjectMocks
    private OfferController offerController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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
        when(offerService.addOrEditOffer(any(OfferDTO.class), anyString())).thenReturn(offerDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Test Offer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Offer"))
                .andExpect(jsonPath("$.offerConfigurations[0].description").value("Test Configuration"))
                .andExpect(jsonPath("$.offerConfigurations[0].selectedOptions.SIZE[0]").value("BIG"));

        verify(offerService, times(1)).addOrEditOffer(any(OfferDTO.class), anyString());
    }

    @Test
    @WithMockUser
    void editConfiguration_ShouldReturnUpdatedConfiguration() throws Exception {
        // Given
        OfferConfigurationDTO configDTO = OfferConfigurationDTO.builder().description("Updated Configuration").build();
        when(offerService.editConfiguration(anyLong(), any(OfferConfigurationDTO.class))).thenReturn(configDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/offer/configuration/1/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"Updated Configuration\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Configuration"));

        verify(offerService, times(1)).editConfiguration(anyLong(), any(OfferConfigurationDTO.class));
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
}

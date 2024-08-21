package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.service.user.CaretakerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CaretakerControllerTest {

    private static final String RATING_BODY = "{\"rating\": %d, \"comment\": \"%s\"}";

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

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private CaretakerService caretakerService;

    @Autowired
    private MockMvc mockMvc;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCaretakers_shouldReturnFilteredResults() throws Exception {
        CaretakerDTO caretakerDTO1 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("John Doe").build())
                .build();

        CaretakerDTO caretakerDTO2 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("Jane Doe").build())
                .build();

        List<CaretakerDTO> caretakerDTOs = List.of(caretakerDTO1, caretakerDTO2);
        Page<CaretakerDTO> page =
                new PageImpl<>(caretakerDTOs, PageRequest.of(0, 10), caretakerDTOs.size());

        when(caretakerService.getCaretakers(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/caretaker")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].accountData.name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].accountData.name").value("Jane Doe"));
    }

    @ParameterizedTest
    @MethodSource("provideRatingData")
    @WithMockUser(username = "client")
    void rateCaretaker(String body, ResultMatcher expectedResponse) throws Exception {
        mockMvc.perform(post("/api/caretaker/1/rating")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedResponse);
    }

    @Test
    @WithMockUser(username = "client")
    void deleteRating() throws Exception {
        mockMvc.perform(delete("/api/caretaker/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRatingsOfCaretaker() throws Exception {
        mockMvc.perform(get("/api/caretaker/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private static Stream<Arguments> provideRatingData() {
        return Stream.of(
                Arguments.of(String.format(RATING_BODY, 5, "Great service!"), status().isOk()),
                Arguments.of(String.format(RATING_BODY, 3, ""), status().isOk()),
                Arguments.of(String.format(RATING_BODY, 1, ""), status().isOk()),
                Arguments.of(String.format(RATING_BODY, 6, "Great service!"), status().isBadRequest()),
                Arguments.of(String.format(RATING_BODY, 0, "Great service!"), status().isBadRequest()),
                Arguments.of("{\"comment\": \"comment\"}", status().isBadRequest()),
                Arguments.of("{\"rating\": 4}", status().isBadRequest())
        );
    }

    @Test
    @WithMockUser
    void addCaretaker_ShouldReturnCreatedCaretaker() throws Exception {
        // Given

        CaretakerDTO caretakerDTO = CaretakerDTO.builder()
                .phoneNumber("123456789")
                .description("Test description")
                .build();

        when(caretakerService.addCaretaker(any(CreateCaretakerDTO.class), anyString())).thenReturn(caretakerDTO);

        // When and Then
        mockMvc.perform(post("/api/caretaker/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(CREATE_CARETAKER_BODY,
                                "123456789",
                                "Test description",
                                "City",
                                "00-000",
                                "MAZOWIECKIE",
                                "Street",
                                "10",
                                "20")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.description").value("Test description"));

        verify(caretakerService, times(1)).addCaretaker(any(CreateCaretakerDTO.class), anyString());
    }

    @Test
    @WithMockUser
    void editCaretaker_ShouldReturnUpdatedCaretaker() throws Exception {
        // Given

        CaretakerDTO caretakerDTO = CaretakerDTO.builder()
                .phoneNumber("987654321")
                .description("Updated description")
                .build();

        when(caretakerService.editCaretaker(any(UpdateCaretakerDTO.class), anyString())).thenReturn(caretakerDTO);

        // When and Then
        mockMvc.perform(patch("/api/caretaker/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(UPDATE_CARETAKER_BODY,
                                "987654321",
                                "Updated description",
                                "New City",
                                "11-111",
                                "PODLASKIE",
                                "New Street",
                                "11",
                                "21")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("987654321"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(caretakerService, times(1)).editCaretaker(any(UpdateCaretakerDTO.class), anyString());
    }
}

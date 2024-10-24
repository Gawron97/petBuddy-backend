package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CaretakerControllerTest {

    private static final String RATING_BODY = "{\"rating\": %d, \"comment\": \"%s\"}";

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

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
        CaretakerDTO caretakerComplexInfoDTO1 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("John Doe").build())
                .build();

        CaretakerDTO caretakerComplexInfoDTO2 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("Jane Doe").build())
                .build();

        List<CaretakerDTO> caretakerComplexInfoDTOS = List.of(caretakerComplexInfoDTO1, caretakerComplexInfoDTO2);
        Page<CaretakerDTO> page = new PageImpl<>(
                caretakerComplexInfoDTOS,
                PageRequest.of(0, 10), caretakerComplexInfoDTOS.size()
        );

        when(caretakerService.getCaretakers(any(), any(), any())).thenReturn(page);

        mockMvc.perform(post("/api/caretaker")
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
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(expectedResponse);
    }

    @Test
    @WithMockUser(username = "client")
    void deleteRating() throws Exception {
        mockMvc.perform(delete("/api/caretaker/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
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
    void getCaretaker_shouldReturnCaretakerDetails() throws Exception {
        // Given
        String caretakerEmail = "johndoe@example.com";
        CaretakerComplexInfoDTO caretakerComplexInfoDTO = CaretakerComplexInfoDTO.builder()
                .accountData(AccountDataDTO.builder()
                        .email(caretakerEmail)
                        .name("John Doe")
                        .build())
                .description("Experienced pet caretaker")
                .build();

        when(caretakerService.getCaretaker(caretakerEmail)).thenReturn(caretakerComplexInfoDTO);

        // When Then
        mockMvc.perform(get("/api/caretaker/{caretakerEmail}", caretakerEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value(caretakerEmail))
                .andExpect(jsonPath("$.accountData.name").value("John Doe"))
                .andExpect(jsonPath("$.description").value("Experienced pet caretaker"));
    }

    @Test
    void getCaretaker_whenCaretakerNotExists_shouldThrowNotFound() throws Exception {
        // Given
        String caretakerEmail = "johndoe@example.com";

        when(caretakerService.getCaretaker(caretakerEmail)).thenThrow(NotFoundException.class);

        // When Then
        mockMvc.perform(get("/api/caretaker/{caretakerEmail}", caretakerEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


}

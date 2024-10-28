package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.rating.RatingService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RatingControllerTest {

    private static final String RATING_BODY = "{\"rating\": %d, \"comment\": \"%s\"}";

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("provideRatingData")
    @WithMockUser(username = "client")
    void rateCaretaker(String body, ResultMatcher expectedResponse) throws Exception {
        when(ratingService.rateCaretaker(anyString(), anyString(), anyLong(), anyInt(), anyString())).thenReturn(null);
        mockMvc.perform(post("/api/rating/caretakerEmail/1")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(expectedResponse);
    }

    @Test
    @WithMockUser(username = "client")
    void deleteRating() throws Exception {
        when(ratingService.deleteRating(anyString(), anyString(), anyLong())).thenReturn(null);
        mockMvc.perform(delete("/api/rating/caretakerEmail/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(ROLE_HEADER_NAME, Role.CLIENT))
                .andExpect(status().isOk());
    }

    @Test
    void getRatingsOfCaretaker() throws Exception {
        when(ratingService.getRatings(any(), anyString())).thenReturn(null);
        mockMvc.perform(get("/api/rating/caretakerEmail")
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

}

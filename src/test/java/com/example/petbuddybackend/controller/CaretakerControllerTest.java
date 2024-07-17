package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.service.user.CaretakerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CaretakerControllerTest {

    @MockBean
    private CaretakerService caretakerService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCaretakers_shouldReturnFilteredResults() throws Exception {
        var caretakerDTO1 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("John Doe").build())
                .build();

        var caretakerDTO2 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("Jane Doe").build())
                .build();

        var caretakers = List.of(caretakerDTO1, caretakerDTO2);
        var page = new PageImpl<>(caretakers, PageRequest.of(0, 10), caretakers.size());
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
}

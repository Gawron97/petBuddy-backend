package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CaretakerControllerTest {

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

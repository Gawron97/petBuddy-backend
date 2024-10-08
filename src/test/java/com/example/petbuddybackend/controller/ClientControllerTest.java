package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Value("${header-name.role}")
    private String roleHeaderName;

    @Test
    @WithMockUser(username = "clientEmail")
    void getClient_shouldReturnClient() throws Exception {

        when(clientService.getClient("clientEmail")).thenReturn(ClientDTO.builder().
                accountData(AccountDataDTO.builder()
                        .email("clientEmail")
                        .build())
                .build()
        );

        mockMvc.perform(get("/api/client")
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value("clientEmail"));

    }

    @Test
    @WithMockUser(username = "clientEmail")
    void addFollowingCaretaker_shouldAddCaretakersToFollowingList() throws Exception {

        // Given
        String caretakerEmail = "caretaker1@example.com";
        when(clientService.addFollowingCaretaker("clientEmail", caretakerEmail)).thenReturn(Set.of(caretakerEmail));

        // When Then
        mockMvc.perform(post("/api/client/follow/".concat(caretakerEmail))
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value(caretakerEmail));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void addFollowingCaretaker_whenArgumentIsBad_shouldThrowIllegalActionException() throws Exception {

        // Given
        String caretakerEmail = "aretaker1@example.com";

        when(clientService.addFollowingCaretaker("clientEmail", caretakerEmail)).thenThrow(IllegalActionException.class);

        // When Then
        mockMvc.perform(post("/api/client/follow/".concat(caretakerEmail))
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void removeFollowingCaretaker_shouldRemoveCaretakersFromFollowingList() throws Exception {

        // Given
        String caretakerEmail = "caretaker1@example.com";

        when(clientService.removeFollowingCaretaker("clientEmail", caretakerEmail)).thenReturn(Set.of("caretaker3@example.com"));

        // When Then
        mockMvc.perform(delete("/api/client/unfollow/".concat(caretakerEmail))
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("caretaker3@example.com"));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void removeFollowingCaretaker_whenArgumentsAreBad_shouldThrowIllegalActionException() throws Exception {

        // Given
        String caretakerEmail = "notFollowed@example.com";

        when(clientService.removeFollowingCaretaker("clientEmail", caretakerEmail)).thenThrow(IllegalActionException.class);

        // When Then
        mockMvc.perform(delete("/api/client/unfollow/".concat(caretakerEmail))
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void getFollowedCaretakers_shouldReturnProperResponse() throws Exception {

        //Given
        Set<CaretakerDTO> caretakers = Set.of(
                CaretakerDTO.builder()
                        .accountData(
                                AccountDataDTO.builder()
                                        .email("caretaker1@email")
                                        .name("caretaker1")
                                        .surname("caretaker1")
                                        .build()
                        )
                        .animals(List.of("DOG"))
                        .build(),
                CaretakerDTO.builder()
                        .accountData(
                                AccountDataDTO.builder()
                                        .email("caretaker2@email")
                                        .name("caretaker2")
                                        .surname("caretaker2")
                                        .build()
                        )
                        .animals(List.of("CAT"))
                        .build()
        );

        when(clientService.getFollowedCaretakers("clientEmail")).thenReturn(caretakers);

        //When Then
        mockMvc.perform(get("/api/client/followed-caretakers")
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

    }

}

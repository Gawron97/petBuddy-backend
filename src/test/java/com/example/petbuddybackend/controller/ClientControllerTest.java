package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.ClientComplexInfoDTO;
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
    void addFollowingCaretakers_shouldAddCaretakersToFollowingList() throws Exception {

        // Given
        Set<String> caretakerEmails = Set.of("caretaker1@example.com", "caretaker2@example.com");

        ClientComplexInfoDTO clientComplexInfoDTO = ClientComplexInfoDTO.builder()
                .accountData(AccountDataDTO.builder()
                        .email("clientEmail")
                        .build())
                .followingCaretakersEmails(caretakerEmails)
                .build();

        when(clientService.addFollowingCaretakers("clientEmail", caretakerEmails)).thenReturn(clientComplexInfoDTO);

        // When Then
        mockMvc.perform(post("/api/client/add-following-caretakers")
                        .header(roleHeaderName, Role.CLIENT)
                        .param("caretakerEmails", "caretaker1@example.com", "caretaker2@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value("clientEmail"))
                .andExpect(jsonPath("$.followingCaretakersEmails").isArray())
                .andExpect(jsonPath("$.followingCaretakersEmails[0]").value(caretakerEmails.toArray()[0]))
                .andExpect(jsonPath("$.followingCaretakersEmails[1]").value(caretakerEmails.toArray()[1]));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void addFollowingCaretakers_whenArgumentAreBad_shouldThrowIllegalActionException() throws Exception {

        // Given
        Set<String> caretakerEmails = Set.of("caretaker1@example.com", "caretaker2@example.com");

        when(clientService.addFollowingCaretakers("clientEmail", caretakerEmails)).thenThrow(IllegalActionException.class);

        // When Then
        mockMvc.perform(post("/api/client/add-following-caretakers")
                        .header(roleHeaderName, Role.CLIENT)
                        .param("caretakerEmails", "caretaker1@example.com", "caretaker2@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void removeFollowingCaretakers_shouldRemoveCaretakersFromFollowingList() throws Exception {

        // Given
        Set<String> caretakerEmails = Set.of("caretaker1@example.com", "caretaker2@example.com");

        ClientComplexInfoDTO clientComplexInfoDTO = ClientComplexInfoDTO.builder()
                .accountData(AccountDataDTO.builder()
                        .email("clientEmail")
                        .build())
                .followingCaretakersEmails(Set.of("caretaker3@example.com"))
                .build();

        when(clientService.removeFollowingCaretakers("clientEmail", caretakerEmails)).thenReturn(clientComplexInfoDTO);

        // When Then
        mockMvc.perform(post("/api/client/remove-following-caretakers")
                        .header(roleHeaderName, Role.CLIENT)
                        .param("caretakerEmails", "caretaker1@example.com", "caretaker2@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value("clientEmail"))
                .andExpect(jsonPath("$.followingCaretakersEmails").isArray())
                .andExpect(jsonPath("$.followingCaretakersEmails[0]").value("caretaker3@example.com"));
    }

    @Test
    @WithMockUser(username = "clientEmail")
    void removeFollowingCaretakers_whenArgumentsAreBad_shouldThrowIllegalActionException() throws Exception {

        // Given
        Set<String> caretakerEmails = Set.of("caretaker1@example.com", "caretaker2@example.com");

        when(clientService.removeFollowingCaretakers("clientEmail", caretakerEmails)).thenThrow(IllegalActionException.class);

        // When Then
        mockMvc.perform(post("/api/client/remove-following-caretakers")
                        .header(roleHeaderName, Role.CLIENT)
                        .param("caretakerEmails", "caretaker1@example.com", "caretaker2@example.com"))
                .andExpect(status().isBadRequest());
    }


}

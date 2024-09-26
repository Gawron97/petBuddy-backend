package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.user.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

        mockMvc.perform(get("/client")
                        .header(roleHeaderName, Role.CLIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value("clientEmail"));

    }

}

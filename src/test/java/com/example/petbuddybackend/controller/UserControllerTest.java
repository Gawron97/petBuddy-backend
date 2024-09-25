package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.user.UserProfiles;
import com.example.petbuddybackend.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAvailableUserProfiles_shouldReturnAvailableUserProfiles() throws Exception {
        // given
        String username = "testuser";

        // when
        when(userService.getUserProfiles(username)).thenReturn(
                UserProfiles.builder()
                        .email(username)
                        .hasClientProfile(true)
                        .hasCaretakerProfile(true)
                        .build()
        );

        // then
        mockMvc.perform(get("/user/available-profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(username))
                .andExpect(jsonPath("$.hasClientProfile").value(true))
                .andExpect(jsonPath("$.hasCaretakerProfile").value(true));
    }

}

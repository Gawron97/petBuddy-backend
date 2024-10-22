package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Test
    @WithMockUser(roles = "caretakerEmail")
    void getUnreadNotifications_shouldReturnProperAnswer() throws Exception {

        when(notificationService.getUnreadNotifications(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/notifications")
                        .header(TIMEZONE_HEADER_NAME, "UTC")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

    }

}

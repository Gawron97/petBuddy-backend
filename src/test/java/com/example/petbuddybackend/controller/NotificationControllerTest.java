package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private NotificationService notificationService;

    @Test
    @WithMockUser("caretakerEmail")
    void getUnreadNotifications_shouldReturnProperAnswer() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SimplyNotificationDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(notificationService.getUnreadNotifications(any(), any(), any(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/notifications")
                        .header(TIMEZONE_HEADER_NAME, "UTC")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

    }

    @Test
    @WithMockUser("caretakerEmail")
    void markNotificationAsRead_shouldReturnProperAnswer() throws Exception {
        when(notificationService.markNotificationAsRead(anyLong(), any(), any()))
                .thenReturn(SimplyNotificationDTO.builder().build());

        mockMvc.perform(patch("/api/notifications/{notificationId}", 1L)
                        .header(TIMEZONE_HEADER_NAME, "UTC")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser("caretakerEmail")
    void markNotificationsAsRead_shouldReturnProperAnswer() throws Exception {

        mockMvc.perform(post("/api/notifications/mark-read")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk());
    }
}

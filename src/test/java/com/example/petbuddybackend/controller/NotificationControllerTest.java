package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private CaretakerNotificationRepository caretakerNotificationRepository;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser("caretakerEmail")
    void getUnreadNotifications_shouldReturnProperAnswer() throws Exception {

        when(notificationService.getUnreadNotifications(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

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
                .thenReturn(NotificationDTO.builder().build());
        when(caretakerNotificationRepository.findById(anyLong())).thenReturn(Optional.of(new CaretakerNotification()));

        mockMvc.perform(patch("/api/notifications/" + 1L)
                        .header(TIMEZONE_HEADER_NAME, "UTC")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk());
    }

}

package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.service.chat.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
public class ChatControllerTest {

    @Value("${timezone.header-name}")
    private String TIMEZONE_HEADER_NAME;

    @MockBean
    private ChatService chatService;

    @Autowired
    private MockMvc mockMvc;

    private Page<ChatMessageDTO> expectedPage;

    @BeforeEach
    void setUp() {
        ChatMessageDTO chatMessageDTO1 = ChatMessageDTO.builder()
                .chatId(1L)
                .senderEmail("sender1")
                .build();

        ChatMessageDTO chatMessageDTO2 = ChatMessageDTO.builder()
                .chatId(1L)
                .senderEmail("sender2")
                .build();

        List<ChatMessageDTO> chatMessageDTOs = List.of(chatMessageDTO1, chatMessageDTO2);
        expectedPage = new PageImpl<>(
                chatMessageDTOs,
                PageRequest.of(0, 10), chatMessageDTOs.size()
        );
    }

    @Test
    @WithMockUser
    void getChatMessages_shouldSucceed() throws Exception {

        when(chatService.getChatMessages(any(), any(), any())).thenReturn(expectedPage);

        mockMvc.perform(get("/api/chat/1/messages")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].senderEmail").value("sender1"))
                .andExpect(jsonPath("$.content[1].senderEmail").value("sender2"));
    }

    @Test
    @WithMockUser
    void getChatMessages_includeTimeZone_shouldSucceed() throws Exception {

        when(chatService.getChatMessages(any(), any(), any(), any())).thenReturn(expectedPage);

        mockMvc.perform(get("/api/chat/1/messages")
                        .param("page", "0")
                        .param("size", "10")
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].senderEmail").value("sender1"))
                .andExpect(jsonPath("$.content[1].senderEmail").value("sender2"));
    }
}

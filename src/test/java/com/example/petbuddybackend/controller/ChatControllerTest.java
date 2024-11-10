package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.user.Role;
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

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerTest {

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @MockBean
    private ChatService chatService;

    @Autowired
    private MockMvc mockMvc;

    private Page<ChatMessageDTO> expectedMessagePage;
    private Page<ChatRoomDTO> expectedChatRoomPage;
    private ChatRoomDTO expectedChatRoom;
    private ChatMessageDTO expectedMessage;

    @BeforeEach
    void setUp() {
        expectedMessage = ChatMessageDTO.builder()
                .id(1L)
                .chatId(1L)
                .senderEmail("sender1")
                .build();

        ChatMessageDTO chatMessageDTO2 = ChatMessageDTO.builder()
                .id(2L)
                .chatId(2L)
                .senderEmail("sender2")
                .build();

        List<ChatMessageDTO> chatMessageDTOs = List.of(expectedMessage, chatMessageDTO2);
        expectedMessagePage = new PageImpl<>(
                chatMessageDTOs,
                PageRequest.of(0, 10), chatMessageDTOs.size()
        );

        expectedChatRoom = ChatRoomDTO.builder()
                .id(1L)
                .chatterEmail("chatter1")
                .chatterName("name1")
                .chatterSurname("surname1")
                .lastMessage("lastMessage1")
                .lastMessageCreatedAt(ZonedDateTime.now())
                .build();

        expectedChatRoomPage = new PageImpl<>(
                List.of(expectedChatRoom),
                PageRequest.of(0, 10), 1
        );
    }

    @Test
    @WithMockUser
    void getChatRooms_includeTimeZone_shouldSucceed() throws Exception {
        when(chatService.getChatRoomsByParticipantEmail(any(), any(), any(), any()))
                .thenReturn(expectedChatRoomPage);

        mockMvc.perform(get("/api/chat")
                        .param("page", "0")
                        .param("size", "10")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER.name())
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("1"));
    }

    @Test
    @WithMockUser
    void getChatMessages_includeTimeZone_shouldSucceed() throws Exception {
        when(chatService.getChatMessagesByParticipantEmail(any(), any(), any(), any())).thenReturn(expectedMessagePage);

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

    @Test
    @WithMockUser
    void createChatRoomWithMessage_includesTimeZone_shouldSucceed() throws Exception {
        when(chatService.createChatRoomWithMessage(any(), any(), any(), any(), any()))
                .thenReturn(expectedMessage);

        mockMvc.perform(post("/api/chat/caretaker@example.com")
                        .param("page", "0")
                        .param("size", "10")
                        .content("{ \"content\": \"message\" }")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER.name())
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser
    void getChatRoom_shouldSucceed() throws Exception {
        when(chatService.getChatRoomWithParticipant(any(), any(), any(), any()))
                .thenReturn(expectedChatRoom);

        mockMvc.perform(get("/api/chat/caretaker@example.com")
                        .header(ROLE_HEADER_NAME, Role.CARETAKER.name())
                        .header(TIMEZONE_HEADER_NAME, "Europe/Warsaw")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser
    void testGetUnreadChatsNumber_shouldReturnProperAnswer() throws Exception {
        mockMvc.perform(get("/api/chat/unread/count"))
                .andExpect(status().isOk());
    }

}

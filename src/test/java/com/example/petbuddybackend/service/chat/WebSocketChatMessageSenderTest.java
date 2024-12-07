package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationSend;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WebSocketChatMessageSenderTest {

    private static final String CLIENT_EMAIL = "client@email";
    private static final String CARETAKER_EMAIL = "caretaker@email";

    @Value("${url.chat.topic.send-url}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Autowired
    private WebSocketChatMessageSender chatSessionService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private ChatService chatService;

    @MockBean
    private WebSocketSessionService webSocketSessionService;

    private ChatRoom chatRoom;
    private SimpSession clientSession;
    private SimpSession caretakerSession;

    @BeforeEach
    void setUp() {
        chatRoom = ChatRoom.builder()
                .id(1L)
                .client(MockUserProvider.createMockClient(CLIENT_EMAIL))
                .caretaker(MockUserProvider.createMockCaretaker(CARETAKER_EMAIL))
                .build();

        clientSession = mock(SimpSession.class);
        caretakerSession = mock(SimpSession.class);
        SimpSubscription caretakerSub = mock(SimpSubscription.class);
        SimpSubscription clientSub = mock(SimpSubscription.class);

        when(clientSub.getDestination())
                .thenReturn("/user" + String.format(CHAT_TOPIC_URL_PATTERN, chatRoom.getId()));

        when(clientSession.getSubscriptions())
                .thenReturn(Set.of(clientSub));

        when(caretakerSub.getDestination())
                .thenReturn("/user" + String.format(CHAT_TOPIC_URL_PATTERN, chatRoom.getId()));

        when(caretakerSession.getSubscriptions())
                .thenReturn(Set.of(caretakerSub));

        when(caretakerSession.getId())
                .thenReturn("subId2");

        when(clientSession.getId())
                .thenReturn("subId1");
    }

    @Test
    void testSendMessage_sendingMessageNotification_shouldSucceed() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CLIENT_EMAIL)))
                .thenReturn(Set.of(clientSession));
        when(webSocketSessionService.getUserSessions(eq(CARETAKER_EMAIL)))
                .thenReturn(Set.of(caretakerSession));
        when(webSocketSessionService.getTimezoneOrDefault(any(String.class)))
                .thenReturn(ZoneId.systemDefault());

        ChatMessageDTO message = ChatMessageDTO.builder()
                .createdAt(ZonedDateTime.now())
                .senderEmail(CLIENT_EMAIL)
                .content("Hello")
                .build();

        chatSessionService.sendMessages(chatRoom, new ChatNotificationSend(message));

        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CLIENT_EMAIL), any(), any(ChatNotificationSend.class), any(Map.class));
        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CARETAKER_EMAIL), any(), any(ChatNotificationSend.class), any(Map.class));
    }
}

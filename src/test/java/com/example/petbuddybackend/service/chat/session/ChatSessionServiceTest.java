package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoined;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeft;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.chat.ChatService;
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
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ChatSessionServiceTest {

    private static final String CLIENT_EMAIL = "client@email";
    private static final String CARETAKER_EMAIL = "caretaker@email";

    @Value("${url.chat.topic.send-url}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Autowired
    private ChatSessionService chatSessionService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private ChatService chatService;

    @MockBean
    private WebSocketSessionService webSocketSessionService;

    @MockBean
    private SimpUserRegistry simpUserRegistry;

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

        chatSessionService.sendMessages(chatRoom, new ChatNotificationMessage(message));

        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CLIENT_EMAIL), any(), any(ChatNotificationMessage.class), any(Map.class));
        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CARETAKER_EMAIL), any(), any(ChatNotificationMessage.class), any(Map.class));
    }

    @Test
    void testOnUserJoinChatRoom_shouldSendNotificationOnFirstJoin() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CLIENT_EMAIL)))
                .thenReturn(Set.of(clientSession));
        when(webSocketSessionService.countChatRoomSessions(eq(CLIENT_EMAIL)))
                .thenReturn(Map.of(chatRoom.getId(), 1));

        chatSessionService.onUserJoinChatRoom(CLIENT_EMAIL, chatRoom.getId());

        verify(chatService)
                .markMessagesAsSeen(eq(chatRoom.getId()), eq(CLIENT_EMAIL));
        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CLIENT_EMAIL), any(), any(ChatNotificationJoined.class), any(Map.class));
    }

    @Test
    void testOnUserUnsubscribe_shouldSendNotification() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CARETAKER_EMAIL)))
                .thenReturn(Set.of(caretakerSession));

        chatSessionService.onUserJoinChatRoom(CLIENT_EMAIL, chatRoom.getId());
        chatSessionService.onUserJoinChatRoom(CARETAKER_EMAIL, chatRoom.getId());

        chatSessionService.onUserUnsubscribe(CLIENT_EMAIL, chatRoom.getId());

        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CARETAKER_EMAIL), any(), any(ChatNotificationLeft.class), any(Map.class));
    }

    @Test
    void testOnUserDisconnect_shouldSendNotification() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CARETAKER_EMAIL)))
                .thenReturn(Set.of(caretakerSession));

        chatSessionService.onUserJoinChatRoom(CARETAKER_EMAIL, chatRoom.getId());
        chatSessionService.onUserJoinChatRoom(CLIENT_EMAIL, chatRoom.getId());

        chatSessionService.onUserDisconnect(CLIENT_EMAIL, Map.of("subId1", chatRoom.getId()));

        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CARETAKER_EMAIL), any(), any(ChatNotificationLeft.class), any(Map.class));
    }

    @Test
    void testisRecipientInChat_noRecipientInChat_shouldReturnIfRecipientIsInChatProperly() {
        SimpSubscription clientSub = mock(SimpSubscription.class);
        when(clientSub.getDestination())
                .thenReturn("/user" + String.format(CHAT_TOPIC_URL_PATTERN, chatRoom.getId()));

        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CLIENT_EMAIL)))
                .thenReturn(Set.of(clientSession));

        when(webSocketSessionService.getUserSubscriptionStartingWithDestination(eq(CLIENT_EMAIL), any()))
                .thenReturn(Set.of(clientSub));
        when(webSocketSessionService.getUserSubscriptionStartingWithDestination(eq(CARETAKER_EMAIL), any()))
                .thenReturn(Set.of());

        assertFalse(chatSessionService.isRecipientInChat(CLIENT_EMAIL, chatRoom));
        assertTrue(chatSessionService.isRecipientInChat(CARETAKER_EMAIL, chatRoom));
    }
}

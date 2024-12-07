package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoin;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeave;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;

import java.util.Map;
import java.util.Set;

import static com.example.petbuddybackend.testutils.mock.MockChatProvider.createMockChatRoom;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ChatEventServiceTest {

    private static final String CLIENT_EMAIL = "client@email";
    private static final String CARETAKER_EMAIL = "caretaker@email";

    private static Client client;
    private static Caretaker caretaker;

    @Value("${url.chat.topic.send-url}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Autowired
    private ChatRoomRepository chatRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ChatEventService chatEventService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

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
        caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker(CARETAKER_EMAIL)
        );

        client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient(CLIENT_EMAIL)
        );

        chatRoom = PersistenceUtils.addChatRoom(
                createMockChatRoom(client, caretaker),
                MockChatProvider.createMockChatMessages(client, caretaker),
                chatRepository,
                chatMessageRepository
        );

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

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testOnUserJoinChatRoom_shouldSendNotificationOnFirstJoin() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CLIENT_EMAIL)))
                .thenReturn(Set.of(clientSession));
        when(webSocketSessionService.countChatRoomSessions(eq(CLIENT_EMAIL)))
                .thenReturn(Map.of(chatRoom.getId(), 1));

        chatEventService.onUserJoinChatRoom(CLIENT_EMAIL, chatRoom.getId());

        verify(chatService)
                .markMessagesAsSeen(eq(chatRoom.getId()), eq(CLIENT_EMAIL));
        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CLIENT_EMAIL), any(), any(ChatNotificationJoin.class), any(Map.class));
    }

    @Test
    void testOnUserUnsubscribe_shouldSendNotification() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CARETAKER_EMAIL)))
                .thenReturn(Set.of(caretakerSession));

        chatEventService.onUserJoinChatRoom(CLIENT_EMAIL, chatRoom.getId());
        chatEventService.onUserJoinChatRoom(CARETAKER_EMAIL, chatRoom.getId());

        chatEventService.onUserUnsubscribe(CLIENT_EMAIL, chatRoom.getId());

        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CARETAKER_EMAIL), any(), any(ChatNotificationLeave.class), any(Map.class));
    }

    @Test
    void testOnUserDisconnect_shouldSendNotification() {
        when(chatService.getChatRoomById(any()))
                .thenReturn(chatRoom);
        when(webSocketSessionService.getUserSessions(eq(CARETAKER_EMAIL)))
                .thenReturn(Set.of(caretakerSession));

        chatEventService.onUserJoinChatRoom(CARETAKER_EMAIL, chatRoom.getId());
        chatEventService.onUserJoinChatRoom(CLIENT_EMAIL, chatRoom.getId());

        chatEventService.onUserDisconnect(CLIENT_EMAIL, Map.of("subId1", chatRoom.getId()));

        verify(simpMessagingTemplate)
                .convertAndSendToUser(eq(CARETAKER_EMAIL), any(), any(ChatNotificationLeave.class), any(Map.class));
    }

    @Test
    void testIsRecipientInChat_noRecipientInChat_shouldReturnIfRecipientIsInChatProperly() {
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

        assertFalse(chatEventService.isRecipientInChat(CARETAKER_EMAIL, chatRoom));
        assertTrue(chatEventService.isRecipientInChat(CLIENT_EMAIL, chatRoom));
    }
}

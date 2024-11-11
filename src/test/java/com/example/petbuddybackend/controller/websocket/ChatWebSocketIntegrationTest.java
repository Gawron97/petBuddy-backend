package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoined;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeft;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationType;
import com.example.petbuddybackend.dto.notification.UnseenChatsNotificationDTO;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.service.block.BlockService;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.testconfig.NoSecurityInjectUserConfig;
import com.example.petbuddybackend.testutils.websocket.WebsocketUtils;
import com.example.petbuddybackend.utils.conversion.serializer.LocalDateTimeDeserializer;
import com.example.petbuddybackend.utils.conversion.serializer.LocalDateTimeSerializer;
import com.example.petbuddybackend.utils.conversion.serializer.ZonedDateTimeDeserializer;
import com.example.petbuddybackend.utils.conversion.serializer.ZonedDateTimeSerializer;
import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test-security-inject-user")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketIntegrationTest {

    private static final String WEBSOCKET_URL_PATTERN = "ws://localhost:%s/ws";
    private static final String SEND_MESSAGE_ENDPOINT = "/app/chat/1";
    private static final int TIMEOUT = 1;
    private static final String CLIENT_USERNAME = "client";
    private static final String CARETAKER_USERNAME = "caretaker";

    @Value("${url.chat.topic.client-subscribe-pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Value("${url.exception.topic.client-subscribe-pattern}")
    public String EXCEPTIONS_PATH;

    @Value("${header-name.timezone}")
    private String HEADER_NAME_TIMEZONE;

    @Value("${header-name.role}")
    private String HEADER_NAME_ROLE;

    @LocalServerPort
    private String PORT;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ChatRoomRepository chatRoomRepository;

    @MockBean
    private BlockService blockService;

    @Autowired
    private MappingJackson2MessageConverter messageConverter;

    private WebSocketStompClient stompClient;
    private BlockingQueue<ChatNotificationMessage> messageBlockingQueue;
    private BlockingQueue<ChatNotificationJoined> joinBlockingQueue;
    private BlockingQueue<ChatNotificationLeft> leaveBlockingQueue;
    private List<StompSession> sessions;
    private BlockingQueue<ApiExceptionResponse> exceptionBlockingQueue;

    private ChatRoom chatRoom;

    @BeforeEach
    void setup() {
        sessions = new ArrayList<>();
        messageBlockingQueue = new LinkedBlockingDeque<>();
        joinBlockingQueue = new LinkedBlockingDeque<>();
        leaveBlockingQueue = new LinkedBlockingDeque<>();
        exceptionBlockingQueue = new LinkedBlockingDeque<>();

        stompClient = new WebSocketStompClient(new SockJsClient(WebsocketUtils.createTransportClient()));
        stompClient.setMessageConverter(messageConverter);

        Client client = Client.builder()
                .email(CLIENT_USERNAME)
                .build();

        Caretaker caretaker = Caretaker.builder()
                .email(CARETAKER_USERNAME)
                .build();

        chatRoom = ChatRoom.builder()
                .id(1L)
                .client(client)
                .caretaker(caretaker)
                .build();

        when(chatService.getChatRoomById(any(Long.class)))
                .thenReturn(chatRoom);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        when(blockService.isBlockedByAny(any(String.class), any(String.class)))
                .thenReturn(false);

        when(chatRoomRepository.existsByIdAndClient_Email(any(Long.class), any(String.class)))
                .thenReturn(true);

        when(chatRoomRepository.existsByIdAndCaretaker_Email(any(Long.class), any(String.class)))
                .thenReturn(true);

        when(chatService.getUnseenChatsNotification(any()))
                .thenReturn(UnseenChatsNotificationDTO.builder().createdAt(ZonedDateTime.now()).build());

        when(chatService.getMessageReceiverEmail(eq(CLIENT_USERNAME), any()))
                .thenReturn(CARETAKER_USERNAME);

        when(chatService.getMessageReceiverEmail(eq(CARETAKER_USERNAME), any()))
                .thenReturn(CLIENT_USERNAME);
    }

    @AfterEach
    void tearDown() {
        sessions.forEach(s -> {
            if (s.isConnected()) {
                s.disconnect();
            }
        });

        stompClient.stop();
    }

    @Test
    @SneakyThrows
    void testSendChatMessage_shouldSucceed() {
        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");
        StompSession stompSession = connectToWebSocket(CLIENT_USERNAME);

        when(chatService.createMessage(any(), any(), any(), any(), eq(false)))
                .thenReturn(createChatMessageDTO(CLIENT_USERNAME));

        // Subscribe to topic and send message
        subscribeToMessageTopic(stompSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        sendMessageToMessageTopic(stompSession, chatMessageSent);

        ChatMessageDTO chatMessageDTO = (messageBlockingQueue.poll(TIMEOUT, SECONDS)).getContent();
        ChatNotificationJoined notificationJoined = joinBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNotNull(chatMessageDTO);
        assertNotNull(notificationJoined);
        assertEquals(CLIENT_USERNAME, notificationJoined.getJoiningUserEmail());

        // No messages should be left in the queue
        assertEquals(0, messageBlockingQueue.drainTo(new ArrayList<>()));
        assertEquals(0, joinBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    @SneakyThrows
    void testSendMessages_usersUseDifferentTimeZones_shouldReceiveMessagesInDifferentTimeZones() {
        String clientTimezone = "+02:00";
        String caretakerTimezone = "+06:00";
        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");

        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        when(chatService.createMessage(any(), any(), any(), any(), eq(true)))
                .thenReturn(createChatMessageDTO(CLIENT_USERNAME));

        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        // Init sessions
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, clientTimezone, Role.CLIENT);
        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, caretakerTimezone, Role.CLIENT);

        // Client sends message
        sendMessageToMessageTopic(clientSession, chatMessageSent);

        // Client and caretaker receive client message
        ChatMessageDTO firstMessage = (messageBlockingQueue.poll(TIMEOUT, SECONDS)).getContent();
        ChatMessageDTO secondMessage = (messageBlockingQueue.poll(TIMEOUT, SECONDS)).getContent();

        assertNotNull(firstMessage);
        assertNotNull(secondMessage);

        // Check if messages were sent in different time zones
        ZoneId firstZone = firstMessage.getCreatedAt().getZone();
        ZoneId secondZone = secondMessage.getCreatedAt().getZone();
        ZoneId clientZone = ZoneId.of(clientTimezone);
        ZoneId caretakerZone = ZoneId.of(caretakerTimezone);

        // One of the messages should be in the client's time zone and the other in the caretaker's time zone
        assertNotEquals(firstZone, secondZone);
        assertTrue(firstZone.equals(clientZone) || secondZone.equals(clientZone));
        assertTrue(firstZone.equals(caretakerZone) || secondZone.equals(caretakerZone));

        // No messages should be left in the queue
        assertEquals(0, messageBlockingQueue.drainTo(new ArrayList<>()));
    }


    @Test
    void testSubscribeToChat_shouldSendJoinNotificationToUsersInChat() throws InterruptedException {
        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        // Subscribe to the chat room topic. Should receive only one notification
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        ChatNotificationJoined firstNotification = joinBlockingQueue.poll(TIMEOUT, SECONDS);

        // Subscribe to the chat room topic. Should receive two notifications
        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, "Europe/Warsaw", Role.CARETAKER);
        ChatNotificationJoined secondNotification = joinBlockingQueue.poll(TIMEOUT, SECONDS);
        ChatNotificationJoined thirdNotification = joinBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNotNull(firstNotification);
        assertEquals(CLIENT_USERNAME, firstNotification.getJoiningUserEmail());

        assertNotNull(secondNotification);
        assertEquals(CARETAKER_USERNAME, secondNotification.getJoiningUserEmail());

        assertNotNull(thirdNotification);
        assertEquals(CARETAKER_USERNAME, thirdNotification.getJoiningUserEmail());

        // No notifications should be left in the queue
        assertEquals(0, joinBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    void testSubscribeToChat_multipleJoins_shouldNotSendDuplicatedJoins() throws InterruptedException {
        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        // Subscribe to the chat room topic. Should receive only one notification
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        joinBlockingQueue.poll(TIMEOUT, SECONDS);

        // Subscribe to the chat room topic. Should receive two notifications
        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, "Europe/Warsaw", Role.CARETAKER);
        joinBlockingQueue.poll(TIMEOUT, SECONDS);
        joinBlockingQueue.poll(TIMEOUT, SECONDS);

        // This should not send a notification
        StompSession anotherClientSession = connectToWebSocket(CLIENT_USERNAME);
        subscribeToMessageTopic(anotherClientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        ChatNotificationJoined shouldBeNull = joinBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNull(shouldBeNull);
        assertEquals(0, joinBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    void testDisconnectFromChat_shouldSendNotificationToOtherUserLeftInChat() throws InterruptedException {
        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        // Subscribe to the chat room topic
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, "Europe/Warsaw", Role.CARETAKER);

        clientSession.disconnect();
        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNotNull(clientLeftNotification);
        assertEquals(CLIENT_USERNAME, clientLeftNotification.getLeavingUserEmail());

        // No notifications should be left in the queue
        assertEquals(0, leaveBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    void testDisconnectFromChat_multipleSubs_shouldNotSendNotification() throws InterruptedException {
        StompSession clientFirstSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession clientSecondSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        // Subscribe to the chat room topic
        subscribeToMessageTopic(clientFirstSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        subscribeToMessageTopic(clientSecondSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);
        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, "Europe/Warsaw", Role.CARETAKER);

        clientFirstSession.disconnect();
        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNull(clientLeftNotification);
        assertEquals(0, leaveBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    @SneakyThrows
    void testUnsubscribeToMessageTopic_shouldCallUnsubscribeAndSendNotification() {
        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        StompSession.Subscription clientSubscription =
                subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);

        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, "Europe/Warsaw", Role.CLIENT);
        clientSubscription.unsubscribe();

        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNotNull(clientLeftNotification);
        assertEquals(0, leaveBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    @SneakyThrows
    void testUnsubscribeToMessageTopic_multipleSubscriptionsToSameTopic_shouldNotSendNotification() {
        StompSession clientFirstSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession clientOtherSession = connectToWebSocket(CLIENT_USERNAME);
        StompSession caretakerSession = connectToWebSocket(CARETAKER_USERNAME);

        StompSession.Subscription clientFirstSubscription =
                subscribeToMessageTopic(clientFirstSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);

        StompSession.Subscription clientSecondSubscription =
                subscribeToMessageTopic(clientOtherSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT);

        subscribeToMessageTopic(caretakerSession, CARETAKER_USERNAME, "Europe/Warsaw", Role.CLIENT);
        clientFirstSubscription.unsubscribe();

        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNull(clientLeftNotification);
    }

    @Test
    @SneakyThrows
    void subscribeToMessageTopic_userNotParticipating_shouldSendExceptionMessage() {
        when(chatRoomRepository.existsByIdAndCaretaker_Email(any(Long.class), any(String.class)))
                .thenReturn(false);
        when(chatRoomRepository.existsByIdAndClient_Email(any(Long.class), any(String.class)))
                .thenReturn(false);

        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        subscribeToExceptionTopic(clientSession, CLIENT_USERNAME , new ExceptionFrameHandler());
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT, new ExceptionFrameHandler());

        ApiExceptionResponse exception = exceptionBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNotNull(exception);
        assertEquals(403, exception.getCode());
        assertEquals("NotParticipateException", exception.getType());
    }

    @Test
    @SneakyThrows
    void subscribeToMessageTopic_chatNotFound_shouldSendExceptionMessage() {
        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        subscribeToExceptionTopic(clientSession, CLIENT_USERNAME , new ExceptionFrameHandler());
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT, new ExceptionFrameHandler());

        ApiExceptionResponse exception = exceptionBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNotNull(exception);
        assertEquals(404, exception.getCode());
        assertEquals("NotFoundException", exception.getType());
    }

    @Test
    @SneakyThrows
    void subscribeToMessageTopic_userBlocked_shouldSendExceptionMessage() {
        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        when(blockService.isBlockedByAny(any(String.class), any(String.class)))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(CLIENT_USERNAME);
        subscribeToExceptionTopic(clientSession, CLIENT_USERNAME , new ExceptionFrameHandler());
        subscribeToMessageTopic(clientSession, CLIENT_USERNAME, "Europe/Warsaw", Role.CLIENT, new ExceptionFrameHandler());

        ApiExceptionResponse exception = exceptionBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNotNull(exception);
        assertEquals(403, exception.getCode());
        assertEquals("BlockedException", exception.getType());
    }

    private void sendMessageToMessageTopic(StompSession stompSession, ChatMessageSent chatMessageSent) {
        StompHeaders sendMessageHeaders = createHeaders(SEND_MESSAGE_ENDPOINT, "Europe/Warsaw", Role.CLIENT);
        stompSession.send(sendMessageHeaders, chatMessageSent);
    }

    private StompSession.Subscription subscribeToMessageTopic(
            StompSession stompSession,
            String connectingUserEmail,
            String timeZone,
            Role role
    ) {
        return subscribeToMessageTopic(
                stompSession,
                connectingUserEmail,
                timeZone,
                role,
                new ChatNotificationFrameHandler()
        );
    }

    private StompSession.Subscription subscribeToMessageTopic(
            StompSession stompSession,
            String connectingUserEmail,
            String timeZone,
            Role role,
            StompFrameHandler stompFrameHandler
    ) {
        NoSecurityInjectUserConfig.injectedUsername = connectingUserEmail;
        String destinationFormatted = String.format(SUBSCRIPTION_URL_PATTERN, 1L);
        StompHeaders headers = createHeaders(destinationFormatted, timeZone, role);
        return WebsocketUtils.subscribeToTopic(stompSession, headers, stompFrameHandler);
    }

    private StompSession.Subscription subscribeToExceptionTopic(
            StompSession session,
            String username,
            ExceptionFrameHandler exceptionFrameHandler
    ) {
        NoSecurityInjectUserConfig.injectedUsername = username;
        String destinationFormatted = EXCEPTIONS_PATH;
        return WebsocketUtils.subscribeToTopic(session, createHeaders(destinationFormatted), exceptionFrameHandler);
    }

    private class ExceptionFrameHandler implements StompFrameHandler {

        private final static ObjectMapper objectMapper;

        static {
            objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();

            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
            module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());

            objectMapper.registerModule(module);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ApiExceptionResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            ApiExceptionResponse payloadConverted = (ApiExceptionResponse) payload;
            exceptionBlockingQueue.add(objectMapper.convertValue(payloadConverted, ApiExceptionResponse.class));
        }
    }

    private class ChatNotificationFrameHandler implements StompFrameHandler {

        private final static ObjectMapper objectMapper;

        static {
            objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();

            module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
            module.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());

            objectMapper.registerModule(module);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return LinkedHashMap.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            LinkedHashMap<String, Object> payloadConverted = (LinkedHashMap<String, Object>) payload;
            ChatNotificationType type = ChatNotificationType.valueOf((String) payloadConverted.get("type"));

            switch(type) {
                case JOIN:
                    joinBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationJoined.class));
                    break;
                case LEAVE:
                    leaveBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationLeft.class));
                    break;
                case SEND:
                    messageBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationMessage.class));
                    break;
            }
        }
    }

    @SneakyThrows
    private StompSession connectToWebSocket(String username) {
        NoSecurityInjectUserConfig.injectedUsername = username;

        StompSession newSession = stompClient.connectAsync(
                String.format(WEBSOCKET_URL_PATTERN, PORT),
                new StompSessionHandlerAdapter() {
                }
        ).get(1, SECONDS);

        sessions.add(newSession);
        return newSession;
    }

    private StompHeaders createHeaders(String destination) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination(destination);
        return headers;
    }

    private StompHeaders createHeaders(String destination, String timezone, Role role) {
        StompHeaders headers = createHeaders(destination);
        headers.add(HEADER_NAME_TIMEZONE, timezone);
        headers.add(HEADER_NAME_ROLE, role.name());
        return headers;
    }

    private ChatMessageDTO createChatMessageDTO(String senderEmail) {
        return ChatMessageDTO.builder()
                .id(1L)
                .createdAt(ZonedDateTime.now())
                .senderEmail(senderEmail)
                .chatId(1L)
                .build();
    }
}

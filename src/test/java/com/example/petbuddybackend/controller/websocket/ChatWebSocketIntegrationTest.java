package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.notification.*;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.testconfig.NoSecurityInjectUserConfig;
import com.example.petbuddybackend.testutils.websocket.WebsocketUtils;
import com.example.petbuddybackend.utils.conversion.serializer.ZonedDateTimeDeserializer;
import com.example.petbuddybackend.utils.conversion.serializer.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test-security-inject-user")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatWebSocketIntegrationTest {

    private static final String WEBSOCKET_URL_PATTERN = "ws://localhost:%s/ws";
    private static final String SEND_MESSAGE_ENDPOINT = "/app/chat/1";
    private static final int TIMEOUT = 1;

    @Value("${url.chat.topic.pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Value("${url.session.topic.pattern}")
    private String SESSION_URL_PATTERN;

    @Value("${header-name.timezone}")
    private String HEADER_NAME_TIMEZONE;

    @Value("${header-name.role}")
    private String HEADER_NAME_ROLE;

    @LocalServerPort
    private String PORT;

    @MockBean
    private ChatService chatService;

    @Autowired
    private MappingJackson2MessageConverter messageConverter;

    private WebSocketStompClient stompClient;
    private BlockingQueue<ChatNotificationMessage> messageBlockingQueue;
    private BlockingQueue<ChatNotificationJoined> joinBlockingQueue;
    private BlockingQueue<ChatNotificationLeft> leaveBlockingQueue;
    private BlockingQueue<ChatNotificationConnected> connectedBlockingQueue;


    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException, TimeoutException {
        messageBlockingQueue = new LinkedBlockingDeque<>();
        joinBlockingQueue = new LinkedBlockingDeque<>();
        leaveBlockingQueue = new LinkedBlockingDeque<>();
        connectedBlockingQueue = new LinkedBlockingDeque<>();

        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(messageConverter);

        when(chatService.createMessage(any(), any(), any(), any()))
                .thenReturn(createChatMessageDTO());

        when(chatService.createCallbackMessageSeen(any(), any()))
                .thenReturn(u -> {});
    }

    @Test
    @SneakyThrows
    void testSendChatMessage_shouldSucceed() {
        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");
        StompSession stompSession = connectToWebSocket();

        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        // Init session
        ChatNotificationConnected chatMessageConnected = getChatNotificationConnected(stompSession, NoSecurityInjectUserConfig.injectedUsername);

        // Subscribe to topic and send message
        subscribeToMessageTopic(stompSession, chatMessageConnected, "Europe/Warsaw", Role.CLIENT);
        sendMessageToMessageTopic(stompSession, chatMessageSent);

        ChatMessageDTO chatMessageDTO = (messageBlockingQueue.poll(TIMEOUT, SECONDS)).getContent();
        assertNotNull(chatMessageDTO);
    }

    @Test
    @SneakyThrows
    void testSendMessages_usersUseDifferentTimeZones_shouldReceiveMessagesInDifferentTimeZones() {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";
        String clientTimezone = "+02:00";
        String caretakerTimezone = "+06:00";
        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");

        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Init session
        ChatNotificationConnected clientMessageConnected = getChatNotificationConnected(clientSession, clientUsername);
        ChatNotificationConnected caretakerMessageConnected = getChatNotificationConnected(caretakerSession, caretakerUsername);

        subscribeToMessageTopic(clientSession, clientMessageConnected, clientTimezone, Role.CLIENT);
        subscribeToMessageTopic(caretakerSession, caretakerMessageConnected, caretakerTimezone, Role.CLIENT);

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
    void testSubscribeToChat_shouldSendNotificationToUsersInChat() throws InterruptedException {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";

        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Init session
        ChatNotificationConnected clientMessageConnected = getChatNotificationConnected(clientSession, clientUsername);
        ChatNotificationConnected caretakerMessageConnected = getChatNotificationConnected(caretakerSession, caretakerUsername);

        // Subscribe to the chat room topic. Should receive only one notification
        subscribeToMessageTopic(clientSession, clientMessageConnected, "Europe/Warsaw", Role.CLIENT);
        ChatNotificationJoined firstNotification = joinBlockingQueue.poll(TIMEOUT, SECONDS);

        // Subscribe to the chat room topic. Should receive two notifications
        subscribeToMessageTopic(caretakerSession, caretakerMessageConnected, "Europe/Warsaw", Role.CARETAKER);
        ChatNotificationJoined secondNotification = joinBlockingQueue.poll(TIMEOUT, SECONDS);
        ChatNotificationJoined thirdNotification = joinBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNotNull(firstNotification);
        assertEquals(clientUsername, firstNotification.getJoiningUserEmail());

        assertNotNull(secondNotification);
        assertEquals(caretakerUsername, secondNotification.getJoiningUserEmail());

        assertNotNull(thirdNotification);
        assertEquals(caretakerUsername, thirdNotification.getJoiningUserEmail());

        // No notifications should be left in the queue
        assertEquals(0, joinBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    void testDisconnectFromChat_shouldSendNotificationToOtherUserLeftInChat() throws InterruptedException {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";

        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Init sessions
        ChatNotificationConnected clientMessageConnected = getChatNotificationConnected(clientSession, clientUsername);
        ChatNotificationConnected caretakerMessageConnected = getChatNotificationConnected(caretakerSession, caretakerUsername);

        // Subscribe to the chat room topic
        subscribeToMessageTopic(clientSession, clientMessageConnected, "Europe/Warsaw", Role.CLIENT);
        subscribeToMessageTopic(caretakerSession, caretakerMessageConnected, "Europe/Warsaw", Role.CARETAKER);

        clientSession.disconnect();
        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(TIMEOUT, SECONDS);

        assertNotNull(clientLeftNotification);
        assertEquals(clientUsername, clientLeftNotification.getLeavingUserEmail());

        // No notifications should be left in the queue
        assertEquals(0, leaveBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    @SneakyThrows
    void handleUnsubscribeToMessageTopic_shouldCallUnsubscribeAndSendNotification() {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";

        when(chatService.isUserInChat(any(Long.class), any(String.class), any(Role.class)))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Init sessions
        ChatNotificationConnected clientMessageConnected = getChatNotificationConnected(clientSession, clientUsername);
        ChatNotificationConnected caretakerMessageConnected = getChatNotificationConnected(caretakerSession, caretakerUsername);

        StompSession.Subscription clientSubscription = subscribeToMessageTopic(clientSession, clientMessageConnected, "Europe/Warsaw", Role.CLIENT);

        subscribeToMessageTopic(caretakerSession, caretakerMessageConnected, "Europe/Warsaw", Role.CLIENT);
        clientSubscription.unsubscribe();

        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNotNull(clientLeftNotification);
    }

    @SneakyThrows
    private ChatNotificationConnected getChatNotificationConnected(StompSession stompSession, String principalUsername) {
        NoSecurityInjectUserConfig.injectedUsername = principalUsername;
        String connectDestination = String.format(SESSION_URL_PATTERN, principalUsername);

        stompSession.subscribe(connectDestination, new ChatNotificationFrameHandler());
        ChatNotificationConnected chatMessageConnected = connectedBlockingQueue.poll(TIMEOUT, SECONDS);
        assertNotNull(chatMessageConnected);

        return chatMessageConnected;
    }

    private void sendMessageToMessageTopic(StompSession stompSession, ChatMessageSent chatMessageSent) {
        StompHeaders sendMessageHeaders = createHeaders(SEND_MESSAGE_ENDPOINT, "Europe/Warsaw", Role.CLIENT);
        stompSession.send(sendMessageHeaders, chatMessageSent);
    }

    private StompSession.Subscription subscribeToMessageTopic(
            StompSession stompSession,
            ChatNotificationConnected chatMessageConnected,
            String timeZone,
            Role role
    ) {
        NoSecurityInjectUserConfig.injectedUsername = chatMessageConnected.getConnectingUserEmail();
        String destinationFormatted = String.format(SUBSCRIPTION_URL_PATTERN, 1L, chatMessageConnected.getSessionId());
        StompHeaders headers = createHeaders(destinationFormatted, timeZone, role);
        return WebsocketUtils.subscribeToTopic(
                stompSession,
                headers,
                new ChatNotificationFrameHandler()
        );
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
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
            ChatNotificationType type = ChatNotificationType.valueOf((String)payloadConverted.get("type"));

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
                case CONNECT:
                    connectedBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationConnected.class));
                    break;
            }
        }
    }

    @SneakyThrows
    private StompSession connectToWebSocket() {
        return stompClient.connectAsync(
                String.format(WEBSOCKET_URL_PATTERN, PORT),
                new StompSessionHandlerAdapter() {}
        ).get(1, SECONDS);
    }

    private StompSession connectToWebSocket(String injectUsername) {
        NoSecurityInjectUserConfig.injectedUsername = injectUsername;
        return connectToWebSocket();
    }

    private StompHeaders createHeaders(String destination, String timezone, Role role) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination(destination);
        headers.add(HEADER_NAME_TIMEZONE, timezone);
        headers.add(HEADER_NAME_ROLE, role.name());
        return headers;
    }

    private ChatMessageDTO createChatMessageDTO() {
        return ChatMessageDTO.builder()
                .id(1L)
                .createdAt(ZonedDateTime.now())
                .chatId(1L)
                .build();
    }
}

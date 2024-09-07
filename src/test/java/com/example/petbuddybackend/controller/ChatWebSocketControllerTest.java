package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.notification.*;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.testconfig.NoSecurityInjectUserConfig;
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
public class ChatWebSocketControllerTest {

    private static final String WEBSOCKET_URL_PATTERN = "ws://localhost:%s/ws";
    private static final String SEND_MESSAGE_ENDPOINT = "/app/chat/1";

    @Value("${url.chat.topic.pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

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


    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException, TimeoutException {
        messageBlockingQueue = new LinkedBlockingDeque<>();
        joinBlockingQueue = new LinkedBlockingDeque<>();
        leaveBlockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(messageConverter);
    }

    @Test
    @SneakyThrows
    void testSendChatMessage_shouldSucceed() {
        String subscribeDestination = String.format(SUBSCRIPTION_URL_PATTERN, 1, NoSecurityInjectUserConfig.injectedUsername);
        ChatMessageDTO returnPayload = createChatMessageDTO();
        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");

        when(chatService.createMessage(any(), any(), any(), any()))
                .thenReturn(returnPayload);

        when(chatService.isUserInChat(any(), any()))
                .thenReturn(true);

        when(chatService.createCallbackMessageSeen(any(), any()))
                .thenReturn(u -> {});

        StompSession stompSession = connectToWebSocket();

        // Subscribe to the chat room topic
        StompHeaders subscribeHeaders = createHeaders(subscribeDestination, "Europe/Warsaw");
        stompSession.subscribe(subscribeHeaders, new ChatNotificationFrameHandler());

        // Send message with headers
        StompHeaders sendMessageHeaders = createHeaders(SEND_MESSAGE_ENDPOINT, "Europe/Warsaw", Role.CLIENT);
        stompSession.send(sendMessageHeaders, chatMessageSent);

        ChatMessageDTO chatMessageDTO = (messageBlockingQueue.poll(2, SECONDS)).getContent();
        assertNotNull(chatMessageDTO);
    }

    @Test
    @SneakyThrows
    void testSendMessages_usersUseDifferentTimeZones_shouldReceiveMessagesInDifferentTimeZones() {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";
        String clientTimezone = "+02:00";
        String caretakerTimezone = "+06:00";
        String subscribeDestinationClient = String.format(SUBSCRIPTION_URL_PATTERN, 1, clientUsername);
        String subscribeDestinationCaretaker = String.format(SUBSCRIPTION_URL_PATTERN, 1, caretakerUsername);

        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");
        ChatMessageDTO returnPayload = createChatMessageDTO();

        when(chatService.createMessage(any(), any(), any(), any()))
                .thenReturn(returnPayload);

        when(chatService.createCallbackMessageSeen(any(), any()))
                .thenReturn(u -> {});

        when(chatService.isUserInChat(any(), any()))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Subscribe to the chat room topic
        StompHeaders clientSubscribeHeaders = createHeaders(subscribeDestinationClient, clientTimezone);
        clientSession.subscribe(clientSubscribeHeaders, new ChatNotificationFrameHandler());
        Thread.sleep(100);

        NoSecurityInjectUserConfig.injectedUsername = caretakerUsername;
        StompHeaders caretakerSubscribeHeaders = createHeaders(subscribeDestinationCaretaker, caretakerTimezone);
        caretakerSession.subscribe(caretakerSubscribeHeaders, new ChatNotificationFrameHandler());
        Thread.sleep(100);

        // Send message with headers
        NoSecurityInjectUserConfig.injectedUsername = clientUsername;
        StompHeaders clientSendHeaders = createHeaders(SEND_MESSAGE_ENDPOINT, clientTimezone, Role.CLIENT);
        clientSession.send(clientSendHeaders, chatMessageSent);

        ChatMessageDTO firstMessage = (messageBlockingQueue.poll(2, SECONDS)).getContent();
        ChatMessageDTO secondMessage = (messageBlockingQueue.poll(2, SECONDS)).getContent();

        assertNotNull(firstMessage);
        assertNotNull(secondMessage);

        // Check if messages were sent in different time zones
        ZoneId firstZone = firstMessage.getCreatedAt().getZone();
        ZoneId secondZone = secondMessage.getCreatedAt().getZone();
        ZoneId clientZone = ZoneId.of(clientTimezone);
        ZoneId caretakerZone = ZoneId.of(caretakerTimezone);
        assertNotEquals(firstZone, secondZone);
        assertTrue(firstZone.equals(clientZone) || secondZone.equals(clientZone));
        assertTrue(firstZone.equals(caretakerZone) || secondZone.equals(caretakerZone));

        // No messages should be left in the queue
        assertEquals(0, messageBlockingQueue.drainTo(new ArrayList<>()));
    }

    @Test
    @SneakyThrows
    void testSubscribeToChatRoom_userIsNotTheParticipant_shouldThrow() {
        String subscribeDestination = String.format(SUBSCRIPTION_URL_PATTERN, 1, NoSecurityInjectUserConfig.injectedUsername);

        when(chatService.isUserInChat(any(), any()))
                .thenReturn(false);

        StompSession stompSession = connectToWebSocket();
        stompSession.subscribe(subscribeDestination, new ChatNotificationFrameHandler());
        Thread.sleep(100);

        assertThrows(IllegalStateException.class, () -> stompSession.send(
                createHeaders(SEND_MESSAGE_ENDPOINT, "Europe/Warsaw", Role.CLIENT),
                new ChatMessageSent("content"))
        );
    }

    @Test
    void testSubscribeToChat_shouldSendNotificationToUsersInChat() throws InterruptedException {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";
        String subscribeDestinationClient = String.format(SUBSCRIPTION_URL_PATTERN, 1, clientUsername);
        String subscribeDestinationCaretaker = String.format(SUBSCRIPTION_URL_PATTERN, 1, caretakerUsername);

        when(chatService.isUserInChat(any(), any()))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Subscribe to the chat room topic
        NoSecurityInjectUserConfig.injectedUsername = clientUsername;
        StompHeaders clientSubscribeHeaders = createHeaders(subscribeDestinationClient, "Europe/Warsaw");
        clientSession.subscribe(clientSubscribeHeaders, new ChatNotificationFrameHandler());

        ChatNotificationJoined firstNotification = joinBlockingQueue.poll(2, SECONDS);

        NoSecurityInjectUserConfig.injectedUsername = caretakerUsername;
        StompHeaders caretakerSubscribeHeaders = createHeaders(subscribeDestinationCaretaker, "Europe/Warsaw");
        caretakerSession.subscribe(caretakerSubscribeHeaders, new ChatNotificationFrameHandler());

        ChatNotificationJoined secondNotification = joinBlockingQueue.poll(2, SECONDS);
        ChatNotificationJoined thirdNotification = joinBlockingQueue.poll(2, SECONDS);

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
    void testUnsubscribeToChat_shouldSendNotificationToOtherUserLeftInChat() throws InterruptedException {
        String clientUsername = "client";
        String caretakerUsername = "caretaker";
        String subscribeDestinationClient = String.format(SUBSCRIPTION_URL_PATTERN, 1, clientUsername);
        String subscribeDestinationCaretaker = String.format(SUBSCRIPTION_URL_PATTERN, 1, caretakerUsername);

        when(chatService.isUserInChat(any(), any()))
                .thenReturn(true);

        StompSession clientSession = connectToWebSocket(clientUsername);
        StompSession caretakerSession = connectToWebSocket(caretakerUsername);

        // Subscribe to the chat room topic
        NoSecurityInjectUserConfig.injectedUsername = clientUsername;
        StompHeaders clientSubscribeHeaders = createHeaders(subscribeDestinationClient, "Europe/Warsaw");
        clientSession.subscribe(clientSubscribeHeaders, new ChatNotificationFrameHandler());

        NoSecurityInjectUserConfig.injectedUsername = caretakerUsername;
        StompHeaders caretakerSubscribeHeaders = createHeaders(subscribeDestinationCaretaker, "Europe/Warsaw");
        caretakerSession.subscribe(caretakerSubscribeHeaders, new ChatNotificationFrameHandler());

        clientSession.disconnect();
        Thread.sleep(200);
        ChatNotificationLeft clientLeftNotification = leaveBlockingQueue.poll(2, SECONDS);

        assertNotNull(clientLeftNotification);
        assertEquals(clientUsername, clientLeftNotification.getLeavingUserEmail());

        // No notifications should be left in the queue
        assertEquals(0, leaveBlockingQueue.drainTo(new ArrayList<>()));
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
                case JOINED:
                    joinBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationJoined.class));
                    break;
                case LEFT:
                    leaveBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationLeft.class));
                    break;
                case MESSAGE:
                    messageBlockingQueue.add(objectMapper.convertValue(payloadConverted, ChatNotificationMessage.class));
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

    private StompHeaders createHeaders(String destination, String timezone) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination(destination);
        headers.add(HEADER_NAME_TIMEZONE, timezone);
        return headers;
    }

    private StompHeaders createHeaders(String destination, String timezone, Role role) {
        StompHeaders headers = createHeaders(destination, timezone);
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

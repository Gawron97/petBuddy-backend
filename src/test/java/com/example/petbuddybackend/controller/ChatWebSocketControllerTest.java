package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.ArrayList;
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
    private static final String SUBSCRIBE_TO_CHAT_PATTERN = "/topic/messages/1";
    private static final String SEND_MESSAGE_ENDPOINT = "/app/chat/1";

    @Value("${header-name.timezone}")
    private String HEADER_NAME_TIMEZONE;

    @Value("${header-name.role}")
    private String HEADER_NAME_ROLE;

    @LocalServerPort
    private String PORT;

    @MockBean
    private ChatService chatService;

    private WebSocketStompClient stompClient;
    private BlockingQueue<ChatMessageDTO> blockingQueue;


    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException, TimeoutException {
        blockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @SneakyThrows
    void testSendChatMessage_shouldSucceed() {
        when(chatService.createMessage(any(), any(), any(), any(), any()))
                .thenReturn(new ChatMessageDTO());

        when(chatService.isUserInChat(any(), any()))
                .thenReturn(true);

        ChatMessageSent chatMessageSent = new ChatMessageSent("hello");
        StompSession stompSession = connectToWebSocket();

        // Subscribe to the chat room topic
        stompSession.subscribe(SUBSCRIBE_TO_CHAT_PATTERN, new ChatMessageFrameHandler());

        // Prepare headers
        StompHeaders headers = new StompHeaders();
        headers.setDestination(SEND_MESSAGE_ENDPOINT);
        headers.add(HEADER_NAME_ROLE, Role.CLIENT.name());

        // Send message with headers
        stompSession.send(headers, chatMessageSent);

        ChatMessageDTO chatMessageDTO = blockingQueue.poll(2, SECONDS);
        assertNotNull(chatMessageDTO);
    }

    @Test
    @SneakyThrows
    void testSubscribeToChatRoom_userIsNotTheParticipant_shouldThrow() {
        when(chatService.isUserInChat(any(), any()))
                .thenReturn(false);

        StompSession stompSession = connectToWebSocket();
        stompSession.subscribe(SUBSCRIBE_TO_CHAT_PATTERN, new ChatMessageFrameHandler());

        StompHeaders headers = new StompHeaders();
        headers.setDestination(SEND_MESSAGE_ENDPOINT);
        headers.add(HEADER_NAME_ROLE, Role.CLIENT.name());

        Thread.sleep(100);

        assertThrows(IllegalStateException.class,
                () -> stompSession.send(headers, new ChatMessageSent("content"))
        );
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class ChatMessageFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ChatMessageDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            blockingQueue.add((ChatMessageDTO) payload);
        }
    }

    @SneakyThrows
    private StompSession connectToWebSocket() {
        return stompClient.connect(
                String.format(WEBSOCKET_URL_PATTERN, PORT),
                new StompSessionHandlerAdapter() {}
        ).get(1, SECONDS);
    }
}

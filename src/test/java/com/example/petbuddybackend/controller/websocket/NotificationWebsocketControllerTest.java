package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.Notification;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.notification.WebsocketNotificationService;
import com.example.petbuddybackend.testutils.PersistenceUtils;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test-security-inject-user")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationWebsocketControllerTest {

    private static final String WEBSOCKET_URL_PATTERN = "ws://localhost:%s/ws";
    private final String USER_EMAIL = "testuser";
    private final String TIMEZONE = "Europe/Warsaw";

    @Value("${url.notification.topic.base}")
    private String SUBSCRIBE_TOPIC;

    @Value("${header-name.timezone}")
    private String HEADER_NAME_TIMEZONE;

    @Autowired
    private WebsocketNotificationService websocketNotificationService;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerNotificationRepository caretakerNotificationRepository;

    @Autowired
    private MappingJackson2MessageConverter messageConverter;

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private BlockingQueue<NotificationDTO> blockingQueue;

    @BeforeEach
    public void setUp() throws Exception {
        blockingQueue = new LinkedBlockingQueue<>();

        stompClient = new WebSocketStompClient(new SockJsClient(WebsocketUtils.createTransportClient()));

        stompClient.setMessageConverter(messageConverter);

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.afterPropertiesSet();
        stompClient.setTaskScheduler(taskScheduler);

    }

    @Test
    void subscribeNotificationTopic_shouldOpenSessionForUser() {

        // Connect to websocket
        StompSession stompSession = connectToWebsocket();

        // Subscribe to notification topic
        StompSession.Subscription subscription = subscribeToNotificationTopic(stompSession);

        // Check if session is opened
        assertEquals(1, websocketNotificationService.getNumberOfSessions(USER_EMAIL));

        // Close session
        subscription.unsubscribe();
        stompSession.disconnect();
        stompClient.stop();

    }

    @Test
    @SneakyThrows
    void testNotificationFlow() {

        // Connect to websocket
        StompSession stompSession = connectToWebsocket();

        // Subscribe to notification topic
        StompSession.Subscription subscription = subscribeToNotificationTopic(stompSession);

        // Send message
        Caretaker caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        Notification notification = PersistenceUtils.addCaretakerNotification(caretakerNotificationRepository, caretaker);
        websocketNotificationService.sendNotification(
                USER_EMAIL,
                notification
        );

        // Check if message was received
        NotificationDTO receivedNotification = blockingQueue.poll(1, TimeUnit.SECONDS);
        assertEquals(notification.getId(), receivedNotification.notificationId());
        assertEquals(notification.getObjectId(), receivedNotification.objectId());
        assertEquals(notification.getObjectType(), receivedNotification.objectType());
        assertEquals(notification.getMessage(), receivedNotification.message());
        assertEquals(Role.CARETAKER, receivedNotification.receiverProfile());

        // Close session
        subscription.unsubscribe();
        stompSession.disconnect();
        stompClient.stop();
    }

    @SneakyThrows
    private StompSession connectToWebsocket() {
        return stompClient.connectAsync(
                String.format(WEBSOCKET_URL_PATTERN, port),
                new StompSessionHandlerAdapter() {}
        ).get(2, TimeUnit.SECONDS);
    }

    private StompSession.Subscription subscribeToNotificationTopic(StompSession stompSession) {
        StompHeaders headers = createHeaders(stompSession);
        return WebsocketUtils.subscribeToTopic(
                stompSession,
                headers,
                new NotificationStompFrameHandler()
        );
    }

    private StompHeaders createHeaders(StompSession stompSession) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination(SUBSCRIBE_TOPIC);
        headers.setSession(stompSession.getSessionId());
        headers.add(HEADER_NAME_TIMEZONE, TIMEZONE);
        return headers;
    }

    private class NotificationStompFrameHandler implements StompFrameHandler {

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
            return NotificationDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            NotificationDTO typedPayload = (NotificationDTO) payload;
            blockingQueue.add(objectMapper.convertValue(typedPayload, NotificationDTO.class));
        }
    }
}

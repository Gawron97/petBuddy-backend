package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.mapper.NotificationMapper;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.time.ZoneId;
import java.util.Collections;

import static com.example.petbuddybackend.testutils.mock.MockNotificationProvider.createMockCaretakerNotification;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WebsocketNotificationSenderTest {

    @Autowired
    private WebsocketNotificationSender wsNotificationSender;

    @Autowired
    private WebSocketSessionService wsSessionService;

    private NotificationMapper notificationMapper = NotificationMapper.INSTANCE;

    @MockBean
    private SimpUserRegistry simpUserRegistry;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Value("${url.notification.topic.send-url}")
    private String NOTIFICATION_BASE_URL;

    private final String userEmail = "caretakerEmail";
    private final String sessionId = "session123";
    private final String userZoneId = "Europe/Warsaw";

    @Test
    public void testSendNotification_whenUserConnected_shouldSendMessageProperly() {

        //Given
        SimpUser simpUser = mock(SimpUser.class);
        SimpSession simpSession = mock(SimpSession.class);

        when(simpUserRegistry.getUser(userEmail)).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(Collections.singleton(simpSession));
        when(simpSession.getId()).thenReturn(sessionId);

        wsSessionService.storeUserTimeZoneWithSession(sessionId, userZoneId);

        //When
        CaretakerNotification notification = createMockCaretakerNotification(createMockCaretaker());
        SimplyNotificationDTO notificationToSend = notificationMapper.mapToSimplyNotificationDTO(notification, ZoneId.of(userZoneId));

        wsNotificationSender.sendNotification(userEmail, notificationToSend);

        // Then
        ArgumentCaptor<SimplyNotificationDTO> notificationDTOCaptor = ArgumentCaptor.forClass(SimplyNotificationDTO.class);
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessageHeaders> headersCaptor = ArgumentCaptor.forClass(MessageHeaders.class);

        verify(simpMessagingTemplate).convertAndSendToUser(
                userCaptor.capture(),
                destinationCaptor.capture(),
                notificationDTOCaptor.capture(),
                headersCaptor.capture()
        );

        assertEquals(userEmail, userCaptor.getValue());
        assertEquals(NOTIFICATION_BASE_URL, destinationCaptor.getValue());
        assertEquals(notification.getId(), notificationDTOCaptor.getValue().getNotificationId());
        assertEquals(notification.getMessageKey(), notificationDTOCaptor.getValue().getMessageKey());
        assertEquals(notification.getArgs(), notificationDTOCaptor.getValue().getArgs());
        assertEquals(notification.getObjectId(), notificationDTOCaptor.getValue().getObjectId());
        assertEquals(notification.getObjectType(), notificationDTOCaptor.getValue().getObjectType());
        assertEquals(Role.CARETAKER, notificationDTOCaptor.getValue().getReceiverProfile());


        MessageHeaders headers = headersCaptor.getValue();
        assertEquals(sessionId, headers.get("simpSessionId"));
    }

    @Test
    public void testSendNotification_whenUserNotConnected_shouldNotSendMessage() {
        //Given
        when(simpUserRegistry.getUser(userEmail)).thenReturn(null);
        CaretakerNotification notification = createMockCaretakerNotification(createMockCaretaker());
        SimplyNotificationDTO notificationToSend = notificationMapper.mapToSimplyNotificationDTO(notification, ZoneId.of(userZoneId));

        //When
        wsNotificationSender.sendNotification(userEmail, notificationToSend);


        //Then
        verify(simpMessagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(), anyMap());
    }
}

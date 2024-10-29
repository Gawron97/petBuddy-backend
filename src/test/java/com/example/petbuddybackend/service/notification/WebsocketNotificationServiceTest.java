package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.user.Role;
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

import java.util.Collections;

import static com.example.petbuddybackend.testutils.mock.MockNotificationProvider.createMockCaretakerNotification;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WebsocketNotificationServiceTest {

    @Autowired
    private WebsocketNotificationService websocketNotificationService;

    @Autowired
    private WebSocketSessionService webSocketSessionService;

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

        webSocketSessionService.storeUserTimeZoneWithSession(sessionId, userZoneId);

        //When
        CaretakerNotification notification = createMockCaretakerNotification(createMockCaretaker());

        websocketNotificationService.sendNotification(userEmail, notification);

        // Then
        ArgumentCaptor<NotificationDTO> notificationDTOCaptor = ArgumentCaptor.forClass(NotificationDTO.class);
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
        assertEquals(notification.getId(), notificationDTOCaptor.getValue().notificationId());
        assertEquals(notification.getMessageKey(), notificationDTOCaptor.getValue().messageKey());
        assertEquals(notification.getArgs(), notificationDTOCaptor.getValue().args());
        assertEquals(notification.getObjectId(), notificationDTOCaptor.getValue().objectId());
        assertEquals(notification.getObjectType(), notificationDTOCaptor.getValue().objectType());
        assertEquals(Role.CARETAKER, notificationDTOCaptor.getValue().receiverProfile());


        MessageHeaders headers = headersCaptor.getValue();
        assertEquals(sessionId, headers.get("simpSessionId"));
    }

    @Test
    public void testSendNotification_whenUserNotConnected_shouldNotSendMessage() {
        //Given
        when(simpUserRegistry.getUser(userEmail)).thenReturn(null);
        CaretakerNotification notification = createMockCaretakerNotification(createMockCaretaker());

        //When
        websocketNotificationService.sendNotification(userEmail, notification);

        //Then
        verify(simpMessagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any(), anyMap());
    }
}

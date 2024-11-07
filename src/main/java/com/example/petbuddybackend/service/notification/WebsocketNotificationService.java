package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.Notification;
import com.example.petbuddybackend.service.mapper.NotificationMapper;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WebsocketNotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;
    private final WebSocketSessionService websocketSessionService;

    @Value("${url.notification.topic.send-url}")
    private String NOTIFICATION_BASE_URL;

    public void sendNotification(String userEmail, Notification notification) {
        if(websocketSessionService.isUserConnected(userEmail)) {
            Set<SimpSession> userSessions = websocketSessionService.getUserSessions(userEmail);
            for(SimpSession session : userSessions) {
                ZoneId timeZone = websocketSessionService.getTimezoneOrDefault(session);

                NotificationDTO notificationToSend = convertNotificationWithMessageTimezone(notification, timeZone);
                simpMessagingTemplate.convertAndSendToUser(
                        userEmail,
                        NOTIFICATION_BASE_URL,
                        notificationToSend,
                        HeaderUtils.createMessageHeadersWithSessionId(session.getId())
                );
            }
        }
    }

    private NotificationDTO convertNotificationWithMessageTimezone(Notification notification, ZoneId timeZone) {
        return notificationMapper.mapToNotificationDTO(notification, timeZone);
    }
}

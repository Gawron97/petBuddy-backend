package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
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
    private final WebSocketSessionService websocketSessionService;

    @Value("${url.notification.topic.send-url}")
    private String NOTIFICATION_BASE_URL;

    public void sendNotification(String userEmail, NotificationDTO notification) {
        if(websocketSessionService.isUserConnected(userEmail)) {
            Set<SimpSession> userSessions = websocketSessionService.getUserSessions(userEmail);
            for(SimpSession session : userSessions) {
                ZoneId timeZone = websocketSessionService.getTimezoneOrDefault(session);
                notification.setCreatedAt(notification.getCreatedAt().withZoneSameInstant(timeZone));

                simpMessagingTemplate.convertAndSendToUser(
                        userEmail,
                        NOTIFICATION_BASE_URL,
                        notification,
                        HeaderUtils.createMessageHeadersWithSessionId(session.getId())
                );
            }
        }
    }

}

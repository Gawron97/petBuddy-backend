package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WebsocketNotificationService {

    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Value("${url.notification.topic.pattern}")
    private String NOTIFICATION_BASE_URL;

    public Integer getNumberOfSessions(String userEmail) {
        return getUserSessions(userEmail).size();
    }

    public void sendNotification(String userEmail, NotificationDTO notification) {
        if(isUserConnected(userEmail)) {
            simpMessagingTemplate.convertAndSendToUser(userEmail, NOTIFICATION_BASE_URL, notification);
        }
    }

    private boolean isUserConnected(String userEmail) {
        SimpUser simpUser = simpUserRegistry.getUser(userEmail);
        return simpUser != null && !simpUser.getSessions().isEmpty();
    }

    private Set<SimpSession> getUserSessions(String userEmail) {
        SimpUser simpUser = simpUserRegistry.getUser(userEmail);
        if(simpUser == null) {
            return Collections.emptySet();
        }
        return simpUser.getSessions();
    }

}

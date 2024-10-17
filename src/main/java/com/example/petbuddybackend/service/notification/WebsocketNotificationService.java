package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.Notification;
import com.example.petbuddybackend.service.mapper.NotificationMapper;
import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebsocketNotificationService {

    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;

    @Value("${url.notification.topic.pattern}")
    private String NOTIFICATION_BASE_URL;

    private final Map<String, ZoneId> sessionsTimeZone = new ConcurrentHashMap<>();

    public Integer getNumberOfSessions(String userEmail) {
        return getUserSessions(userEmail).size();
    }

    public void sendNotification(String userEmail, Notification notification) {
        if(isUserConnected(userEmail)) {
            Set<SimpSession> userSessions = getUserSessions(userEmail);
            for(SimpSession session : userSessions) {
                ZoneId timeZone = sessionsTimeZone.getOrDefault(session.getId(), ZoneId.systemDefault());
                System.out.println("Timezone: " + timeZone);
                NotificationDTO notificationToSend = convertNotificationWithMessageTimezone(notification, timeZone);
                simpMessagingTemplate.convertAndSendToUser(
                        userEmail,
                        NOTIFICATION_BASE_URL,
                        notificationToSend,
                        createHeaders(session.getId())
                );
            }
        }
    }

    public void storeUserTimeZoneWithSession(String sessionId, String zoneId) {
        sessionsTimeZone.put(sessionId, TimeUtils.getOrSystemDefault(zoneId));
    }

    public void removeUserSessionWithTimeZone(String sessionId) {
        sessionsTimeZone.remove(sessionId);
    }

    private NotificationDTO convertNotificationWithMessageTimezone(Notification notification, ZoneId timeZone) {
        return notificationMapper.mapToNotificationDTO(notification, timeZone);
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
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

package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.service.notification.WebsocketNotificationService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Controller purpose is to log information about subscription of /user/topic/notification
 * */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebsocketController {

    private final WebsocketNotificationService websocketNotificationService;

    @Value("${url.notification.topic.base}")
    private String NOTIFICATION_BASE_URL;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(!HeaderUtils.destinationStartsWith(NOTIFICATION_BASE_URL, destination)) {
            return;
        }

        String userEmail = accessor.getUser().getName();
        String sessionId = accessor.getSessionId();
        String zoneId = HeaderUtils.getNativeHeaderSingleValue(accessor, TIMEZONE_HEADER_NAME, String.class);

        websocketNotificationService.storeUserTimeZoneWithSession(sessionId, zoneId);

        log.info("User {} subscribed to {} with number of sessions: {}",
                userEmail, destination, websocketNotificationService.getNumberOfSessions(userEmail));

    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        websocketNotificationService.removeUserSessionWithTimeZone(sessionId);

        log.info("Session {} disconnected", sessionId);

    }


}

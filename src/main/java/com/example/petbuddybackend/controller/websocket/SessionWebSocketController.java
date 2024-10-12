package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.dto.chat.notification.ChatNotificationConnected;
import com.example.petbuddybackend.service.chat.session.ChatSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SessionWebSocketController {

    @Value("${url.session.topic.base}")
    private String SESSION_BASE_URL;

    private final ChatSessionService chatSessionService;

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(!HeaderUtils.destinationStartsWith(SESSION_BASE_URL, destination)) {
            return;
        }

        String username = HeaderUtils.getUser(accessor);
        String sessionId = accessor.getSessionId();

        chatSessionService.sendSessionNotification(username, new ChatNotificationConnected(sessionId, username));
        log.debug("Subscribe triggered by session: {}, at destination: {}", sessionId, destination);
    }
}

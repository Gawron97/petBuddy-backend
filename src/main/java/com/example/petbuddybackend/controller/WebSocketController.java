package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.service.chat.session.context.SessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SessionContext context;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        context.setSessionId(accessor.getSessionId());
        log.debug("Connect triggered by session: {}", accessor.getSessionId());
    }
}

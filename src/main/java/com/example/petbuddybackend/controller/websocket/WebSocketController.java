package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    private final WebSocketSessionService wsMetadataService;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String zoneId = HeaderUtils.getOptionalNativeHeaderSingleValue(accessor, TIMEZONE_HEADER_NAME, String.class)
                .orElse(ZoneId.systemDefault().toString());

        wsMetadataService.storeUserTimeZoneWithSession(accessor.getSessionId(), zoneId);

        log.debug(
                "Event connect; sessionId: {}; user: {}",
                accessor.getSessionId(),
                accessor.getUser() == null ? "null" : accessor.getUser().getName()
        );
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Optional<String> zoneIdOpt = HeaderUtils.getOptionalNativeHeaderSingleValue(
                accessor,
                TIMEZONE_HEADER_NAME,
                String.class
        );

        if(zoneIdOpt.isPresent()) {
            String sessionId = accessor.getSessionId();
            wsMetadataService.storeUserTimeZoneWithSession(sessionId, zoneIdOpt.get());
        }

        log.trace(
                "Event subscription; sessionId: {}; user: {}; destination: {}",
                accessor.getSessionId(),
                accessor.getUser() == null ? "null" : accessor.getUser().getName(),
                accessor.getDestination()
        );
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        wsMetadataService.removeUserSessionWithTimeZone(sessionId);
        log.debug(
                "Event disconnect; sessionId: {}; user: {}",
                sessionId,
                accessor.getUser() == null ? "null" : accessor.getUser().getName()
        );
    }
}

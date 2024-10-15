package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoined;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeft;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.session.ChatSessionService;
import com.example.petbuddybackend.service.chat.session.MessageCallback;
import com.example.petbuddybackend.service.chat.session.context.WebSocketSessionContext;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final int CHAT_ID_INDEX_IN_TOPIC_URL = 3;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @Value("${url.chat.topic.base}")
    private String URL_CHAT_TOPIC_BASE;

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final WebSocketSessionContext sessionContext;

    @MessageMapping("/chat/{chatId}")
    public void sendChatMessage(
            @DestinationVariable Long chatId,
            @Valid @Payload ChatMessageSent message,
            @Headers Map<String, Object> headers,
            Principal principal
    ) {
        String sessionId = HeaderUtils.getSessionId(headers);
        String principalUsername = principal.getName();
        Role acceptRole = HeaderUtils.getNativeHeaderSingleValue(headers, ROLE_HEADER_NAME, Role.class);
        ChatMessageDTO messageDTO = chatService.createMessage(chatId, principalUsername, message, acceptRole);
        MessageCallback callback = chatService.createCallbackMessageSeen(chatId, principalUsername);

        chatSessionService.patchMetadata(chatId, principalUsername, sessionId, headers);
        chatSessionService.sendNotifications(chatId, new ChatNotificationMessage(messageDTO), callback);
        log.debug("Send message triggered by session id: {}", sessionId);
    }

    @EventListener
    public void handleSubscribeToMessageTopic(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(!HeaderUtils.destinationStartsWith(URL_CHAT_TOPIC_BASE, destination)) {
            return;
        }

        String timeZone = HeaderUtils.getOptionalNativeHeaderSingleValue(accessor, TIMEZONE_HEADER_NAME, String.class)
                .orElse(null);

        String username = HeaderUtils.getUser(accessor);
        Long chatId = HeaderUtils.getLongFromDestination(accessor, CHAT_ID_INDEX_IN_TOPIC_URL);
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();

        chatService.updateLastMessageSeen(chatId, username);
        chatSessionService.subscribe(chatId, username, sessionId, TimeUtils.getOrSystemDefault(timeZone), subscriptionId);
        chatSessionService.sendNotifications(chatId, new ChatNotificationJoined(chatId, username));

        log.debug(
                "Event subscribe at {}; sessionId: {}; user: {}",
                URL_CHAT_TOPIC_BASE,
                accessor.getSessionId(),
                accessor.getUser() == null ? "null" : accessor.getUser().getName()
        );
    }

    @EventListener
    public void handleUnsubscribeToMessageTopic(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(!sessionContext.containsSubscriptionId(accessor.getSubscriptionId()) || sessionContext.isEmpty()) {
            return;
        }

        String username = sessionContext.getUsername();
        Long chatId = sessionContext.getChatId();
        String sessionId = sessionContext.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();

        chatSessionService.sendNotifications(chatId, new ChatNotificationLeft(chatId, username));
        chatSessionService.unsubscribe(chatId, username, sessionId, subscriptionId);

        log.debug(
                "Event unsubscribe at {}; sessionId: {}; user: {}",
                URL_CHAT_TOPIC_BASE,
                accessor.getSessionId(),
                accessor.getUser() == null ? "null" : accessor.getUser().getName()
        );
    }

    @EventListener
    public void handleDisconnectFromWebSocket(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if(sessionContext.isEmpty()) {
            return;
        }

        String username = sessionContext.getUsername();
        Long chatId = sessionContext.getChatId();
        chatSessionService.sendNotifications(chatId, new ChatNotificationLeft(chatId, username));

        log.debug(
                "Event disconnect from {}; sessionId: {}; user: {}",
                URL_CHAT_TOPIC_BASE,
                accessor.getSessionId(),
                accessor.getUser() == null ? "null" : accessor.getUser().getName()
        );
    }
}

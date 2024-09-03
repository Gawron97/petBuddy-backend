package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.session.ChatSessionService;
import com.example.petbuddybackend.service.chat.session.MessageCallback;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private static final int CHAT_ID_INDEX_IN_TOPIC_URL = 3;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;

    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chat/{chatId}")
    public void sendChatMessage(
            @DestinationVariable Long chatId,
            @Valid @Payload ChatMessageSent message,
            @Headers Map<String, Object> headers,
            Principal principal
    ) {
        String principalUsername = principal.getName();
        Role acceptRole = HeaderUtils.getHeaderSingleValue(headers, ROLE_HEADER_NAME, Role.class);
        ChatMessageDTO messageDTO = chatService.createMessage(chatId, principalUsername, message, acceptRole);
        MessageCallback callback = chatService.createCallbackMessageSeen(chatId, principalUsername);

        chatSessionService.patchMetadata(chatId, principalUsername, headers);
        chatSessionService.sendMessages(chatId, messageDTO, callback);
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String timeZone = HeaderUtils.getHeaderSingleValue(accessor, TIMEZONE_HEADER_NAME, String.class);
        String username = HeaderUtils.getUser(accessor);
        Long chatId = HeaderUtils.getLongFromDestination(accessor, CHAT_ID_INDEX_IN_TOPIC_URL);

        chatService.updateLastMessageSeen(chatId, username);
        chatSessionService.subscribeIfAbsent(chatId, username, timeZone);
    }

    @EventListener
    public void handleUnsubscription(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = HeaderUtils.getUser(accessor);
        Long chatId = HeaderUtils.getLongFromDestination(accessor, CHAT_ID_INDEX_IN_TOPIC_URL);

        chatSessionService.unsubscribeIfPresent(chatId, username);
    }
}

package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.session.ChatSessionService;
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

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

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
        String username = principal.getName();
        Role acceptRole = HeaderUtils.getHeaderSingleValue(headers, ROLE_HEADER_NAME, Role.class);
        ChatMessageDTO messageDTO = chatService.createMessage(chatId, username, message, acceptRole);

        chatSessionService.patchMetadata(chatId, username, headers);
        chatSessionService.sendMessages(chatId, messageDTO);
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        chatSessionService.subscribeIfAbsent(headerAccessor);
    }

    @EventListener
    public void handleUnsubscription(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        chatSessionService.unsubscribeIfPresent(headerAccessor);
    }
}

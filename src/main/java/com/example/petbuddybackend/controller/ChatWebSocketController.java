package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    private final ChatService chatService;

    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/messages/{chatId}")
    @SendToUser
    public ChatMessageDTO sendChatMessage(
            @DestinationVariable Long chatId,
            @Valid @Payload ChatMessageSent message,
            @Headers Map<String, Object> headers,
            Principal principal
    ) {
        String username = principal.getName();
        Role acceptRole = HeaderUtils.getHeaderSingleValue(headers, ROLE_HEADER_NAME, Role.class);

        // Full support for time zones will added in next PR
        return chatService.createMessage(
                chatId,
                username,
                message,
                acceptRole,
                TimeUtils.getOrSystemDefault("Europe/Warsaw")
        );
    }
}

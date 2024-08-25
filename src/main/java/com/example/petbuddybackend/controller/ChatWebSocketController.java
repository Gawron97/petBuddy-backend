package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.time.TimeUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/messages/{chatId}")
    public ChatMessageDTO sendChatMessage(
            @DestinationVariable Long chatId,
            @Valid @Payload ChatMessageSent message,
            @RequestHeader(value = "${header-name.timezone}", required = false) String acceptTimeZone,
            @RequestHeader(value = "${header-name.role}") Role acceptRole
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return acceptTimeZone != null ?
                chatService.saveMessage(chatId, username, message, acceptRole, TimeUtils.getOrSystemDefault(acceptTimeZone)) :
                chatService.saveMessage(chatId, username, message, acceptRole);
    }
}

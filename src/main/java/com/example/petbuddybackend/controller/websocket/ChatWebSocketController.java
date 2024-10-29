package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.session.ChatSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Value("${url.chat.topic.subscribe-prefix}")
    private String URL_CHAT_TOPIC_BASE;

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;

    @Transactional
    @MessageMapping("/chat/{chatId}")
    public void sendChatMessage(
            @DestinationVariable Long chatId,
            @Valid @Payload ChatMessageSent message,
            @Headers Map<String, Object> headers,
            Principal principal
    ) {
        String sessionId = HeaderUtils.getSessionId(headers);
        log.debug("Send message triggered by session id: {}", sessionId);

        String principalUsername = principal.getName();
        Role acceptRole = HeaderUtils.getNativeHeaderSingleValue(headers, ROLE_HEADER_NAME, Role.class);
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        boolean seenByRecipient = chatSessionService.isRecipientInChat(principalUsername, chatRoom);
        ChatMessageDTO messageDTO = chatService.createMessage(chatRoom, principalUsername, acceptRole, message, seenByRecipient);
        chatSessionService.sendMessages(chatRoom, new ChatNotificationMessage(messageDTO));
    }

    @EventListener
    public void handleSubscribeToMessageTopic(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(!HeaderUtils.destinationStartsWith(URL_CHAT_TOPIC_BASE, destination)) {
            return;
        }

        String username = HeaderUtils.getUser(accessor);
        Long chatId = HeaderUtils.getLongFromDestination(accessor, CHAT_ID_INDEX_IN_TOPIC_URL);
        chatSessionService.onUserJoinChatRoom(username, chatId, accessor.getSubscriptionId());
        chatService.markMessagesAsSeen(chatId, username);

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
        String username = HeaderUtils.getUser(accessor);
        chatSessionService.onUserUnsubscribe(username, accessor.getSubscriptionId());

        log.debug(
                "Event unsubscribe at {}; sessionId: {}; user: {}",
                URL_CHAT_TOPIC_BASE,
                accessor.getSessionId(),
                username
        );
    }

    @EventListener
    public void handleDisconnectFromWebSocket(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = HeaderUtils.getUser(accessor);
        chatSessionService.onUserDisconnect(username);

        log.debug(
                "Event disconnect from {}; sessionId: {}; user: {}",
                URL_CHAT_TOPIC_BASE,
                accessor.getSessionId(),
                username
        );
    }
}

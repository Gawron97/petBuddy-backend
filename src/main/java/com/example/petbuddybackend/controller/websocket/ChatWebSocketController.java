package com.example.petbuddybackend.controller.websocket;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.WebSocketChatMessageSender;
import com.example.petbuddybackend.service.notification.WebsocketNotificationSender;
import com.example.petbuddybackend.service.session.chat.ChatSessionTracker;
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
    private final WebSocketChatMessageSender wsChatMessageSender;
    private final WebsocketNotificationSender wsNotificationSender;
    private final ChatSessionTracker chatSessionTracker;

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
        String recipientUsername = chatService.getMessageReceiverEmail(principalUsername, chatRoom);

        boolean seenByRecipient = wsChatMessageSender.isRecipientInChat(recipientUsername, chatRoom);
        ChatMessageDTO messageDTO = chatService.createMessage(
                chatRoom,
                principalUsername,
                acceptRole,
                message,
                seenByRecipient
        );

        wsChatMessageSender.sendMessages(chatRoom, new ChatNotificationMessage(messageDTO));
        if(!seenByRecipient) {
            wsNotificationSender.sendNotification(
                    recipientUsername,
                    chatService.getUnseenChatsNotification(recipientUsername)
            );
        }
    }

    @EventListener
    public void handleSubscribeToMessageTopic(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        if(!HeaderUtils.destinationStartsWith(URL_CHAT_TOPIC_BASE, destination)) {
            return;
        }

        String subscriberUsername = HeaderUtils.getUser(accessor);
        Long chatId = HeaderUtils.getLongFromDestination(accessor, CHAT_ID_INDEX_IN_TOPIC_URL);

        chatSessionTracker.addSubscription(accessor.getSubscriptionId(), chatId);
        wsChatMessageSender.onUserJoinChatRoom(subscriberUsername, chatId);
        wsNotificationSender.sendNotification(
                subscriberUsername,
                chatService.getUnseenChatsNotification(subscriberUsername)
        );

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
        String subId = accessor.getSubscriptionId();

        Long chatId = chatSessionTracker.removeSubscription(subId);
        wsChatMessageSender.onUserUnsubscribe(username, chatId);

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

        Map<String, Long> subscriptions = chatSessionTracker.getSubscriptions();
        wsChatMessageSender.onUserDisconnect(username, subscriptions);
        chatSessionTracker.clear();

        log.debug(
                "Event disconnect from {}; sessionId: {}; user: {}",
                URL_CHAT_TOPIC_BASE,
                accessor.getSessionId(),
                username
        );
    }
}

package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotification;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationSend;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.service.session.WebSocketSessionService;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketChatMessageSender {

    @Value("${url.chat.topic.send-url}")
    private String CHAT_TOPIC_URL_PATTERN;

    private final ChatMapper chatMapper = ChatMapper.INSTANCE;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final WebSocketSessionService webSocketSessionService;

    public void sendMessages(ChatRoom chatRoom, ChatNotification notification) {
        Long chatId = chatRoom.getId();
        String clientEmail = chatRoom.getClient().getEmail();
        String caretakerEmail = chatRoom.getCaretaker().getEmail();

        webSocketSessionService.getUserSessions(clientEmail)
                .forEach((simpSession) -> sendToUser(chatId, clientEmail, simpSession.getId(), notification));

        webSocketSessionService.getUserSessions(caretakerEmail)
                .forEach((simpSession) -> sendToUser(chatId, caretakerEmail, simpSession.getId(), notification));
    }

    private void sendToUser(Long chatId, String receiverUsername, String sessionId, ChatNotification notification) {
        if(notification instanceof ChatNotificationSend messageNotification) {
            log.trace("Sending message notification to user: {}, session {}", receiverUsername, sessionId);
            notifyWithMessage(chatId, receiverUsername, sessionId, messageNotification);
        } else {
            log.trace("Sending generic notification to user: {}, session {}", receiverUsername, sessionId);
            notifyWithGenericMessage(chatId, receiverUsername, sessionId, notification);
        }
    }

    private void notifyWithMessage(
            Long chatId,
            String receiverUsername,
            String sessionId,
            ChatNotificationSend message
    ) {
        ZoneId zoneId = webSocketSessionService.getTimezoneOrDefault(sessionId);
        convertNotificationMessageTimezone(message, zoneId);
        notifyWithGenericMessage(chatId, receiverUsername, sessionId, message);
    }

    private void notifyWithGenericMessage(
            Long chatId,
            String username,
            String sessionId,
            ChatNotification notification
    ) {
        simpMessagingTemplate.convertAndSendToUser(
                username,
                String.format(CHAT_TOPIC_URL_PATTERN, chatId),
                notification,
                HeaderUtils.createMessageHeadersWithSessionId(sessionId)
        );
    }

    private void convertNotificationMessageTimezone(ChatNotification notification, ZoneId zoneId) {
        ChatNotificationSend notificationMessage = (ChatNotificationSend) notification;
        ChatMessageDTO mappedMessage = chatMapper.mapTimeZone(notificationMessage.getContent(), zoneId);
        notificationMessage.setContent(mappedMessage);
    }
}

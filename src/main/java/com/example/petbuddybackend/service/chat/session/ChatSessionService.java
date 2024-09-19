package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotification;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationConnected;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationType;
import com.example.petbuddybackend.service.chat.session.context.WebSocketSessionContext;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.utils.exception.throweable.SessionNotFoundException;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    public static final String SESSION_FOR_CHAT_ID_MESSAGE = "Session for chat id \"%d\" not found";
    public static final String SESSION_FOR_USER_DATA = "Session for chat id \"%d\", username \"%s\" and session id \"%s\" not found";

    @Value("${url.chat.topic.pattern}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Value("${url.session.topic.pattern}")
    private String SESSION_URL_PATTERN;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatSessionManager chatSessionManager;
    private final ChatMapper chatMapper = ChatMapper.INSTANCE;
    private final WebSocketSessionContext sessionContext;

    public void sendNotifications(Long chatId, ChatNotification notification, MessageCallback callback) {
        ChatRoomSessionMetadata chatRoomMeta = chatSessionManager.find(chatId).orElseThrow(
                () -> new SessionNotFoundException(String.format(SESSION_FOR_CHAT_ID_MESSAGE, chatId))
        );

        chatRoomMeta.forEach(userMetadata -> {
            if(notification.getType().equals(ChatNotificationType.SEND)) {
                convertNotificationMessageTimezone(notification, userMetadata.getZoneId());
            }

            String destination = formatTopicDestination(chatId, userMetadata.getSessionId());
            executeSendNotification(destination, notification);
            callback.onMessageSent(userMetadata.getUsername());
        });
    }

    public void sendNotifications(Long chatId, ChatNotification notification) {
        sendNotifications(chatId, notification, username -> {});
    }

    public void sendSessionNotification(String username, ChatNotificationConnected notification) {
        simpMessagingTemplate.convertAndSend(String.format(SESSION_URL_PATTERN, username), notification);
    }

    public void patchMetadata(Long chatId, String username, String sessionId, Map<String, Object> headers) {
        ChatUserMetadata metadata = chatSessionManager.find(chatId, username, sessionId).orElseThrow(
                () -> new SessionNotFoundException(String.format(SESSION_FOR_USER_DATA, chatId, username, sessionId))
        );

        Optional<String> timeZone = HeaderUtils.getOptionalNativeHeaderSingleValue(headers, TIMEZONE_HEADER_NAME, String.class);

        if(timeZone.isEmpty()) {
            return;
        }

        metadata.setZoneId(TimeUtils.get(timeZone.get()));
    }

    public void subscribe(Long chatId, String username, String sessionId, ZoneId timeZone, String subscriptionId) {
        sessionContext.setContext(chatId, username, chatSessionManager::removeIfExists);
        sessionContext.addSubscriptionId(subscriptionId);
        ChatUserMetadata metadata = new ChatUserMetadata(username, sessionId, timeZone);
        chatSessionManager.put(chatId, metadata);
    }

    public void unsubscribe(Long chatId, String username, String sessionId, String subscriptionId) {
        sessionContext.clearContext();
        sessionContext.removeSubscriptionId(subscriptionId);
        chatSessionManager.removeIfExists(chatId, username, sessionId);
    }

    private void executeSendNotification(String destination, ChatNotification notification) {
        simpMessagingTemplate.convertAndSend(destination, notification);
    }

    private String formatTopicDestination(Long chatId, String username) {
        return String.format(CHAT_TOPIC_URL_PATTERN, chatId, username);
    }

    private void convertNotificationMessageTimezone(ChatNotification notification, ZoneId zoneId) {
        ChatNotificationMessage notificationMessage = (ChatNotificationMessage) notification;
        ChatMessageDTO mappedMessage = chatMapper.mapTimeZone(notificationMessage.getContent(), zoneId);
        notificationMessage.setContent(mappedMessage);
    }
}

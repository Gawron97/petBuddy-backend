package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.notification.ChatNotification;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.service.chat.session.context.ChatSessionContext;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    @Value("${url.chat.topic.pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatSessionManager chatSessionManager;
    private final ChatMapper chatMapper = ChatMapper.INSTANCE;
    private final ChatSessionContext sessionContext;

    public ChatSessionContext getContext() {
        return sessionContext;
    }

    public void sendNotifications(Long chatId, ChatNotification notification, MessageCallback callback) {
        switch(notification.getType()) {
            case MESSAGE:
                executeSendNotificationConvertTimeZone((ChatNotificationMessage) notification, chatId, callback);
                break;
            case JOINED, LEFT:
                executeSendNotification(notification, chatId, callback);
                break;
        }
    }

    public void sendNotifications(Long chatId, ChatNotification notification) {
        sendNotifications(chatId, notification, username -> {});
    }

    public void patchMetadata(Long chatId, String username, Map<String, Object> headers) {
        Optional<String> timeZone = HeaderUtils.getOptionalHeaderSingleValue(headers, TIMEZONE_HEADER_NAME, String.class);
        ChatUserMetadata metadata = chatSessionManager.get(chatId, username);
        timeZone.ifPresent(s -> metadata.setZoneId(TimeUtils.get(s)));
    }

    public void subscribe(Long chatId, String username, String timeZone) {
        sessionContext.setChatId(chatId);
        sessionContext.setUsername(username);
        sessionContext.setCleanupCallback(chatSessionManager::remove);

        ChatUserMetadata metadata = new ChatUserMetadata(username, TimeUtils.getOrSystemDefault(timeZone));
        chatSessionManager.putIfAbsent(chatId, metadata);
    }

    private void executeSendNotificationConvertTimeZone(
            ChatNotificationMessage notification,
            Long chatId,
            MessageCallback callback
    ) {
        chatSessionManager.get(chatId).forEach(userMetadata -> {
            String username = userMetadata.getUsername();

            notification.setContent(
                    chatMapper.mapTimeZone(notification.getContent(), userMetadata.getZoneId())
            );

            simpMessagingTemplate.convertAndSend(formatDestination(chatId, username), notification);
            callback.onMessageSent(username);
        });
    }

    private void executeSendNotification(ChatNotification notification, Long chatId, MessageCallback callback) {
        chatSessionManager.get(chatId).forEach(userMetadata -> {
            String username = userMetadata.getUsername();

            simpMessagingTemplate.convertAndSend(formatDestination(chatId, username), notification);
            callback.onMessageSent(username);
        });
    }

    private String formatDestination(Long chatId, String username) {
        return String.format(SUBSCRIPTION_URL_PATTERN, chatId, username);
    }
}

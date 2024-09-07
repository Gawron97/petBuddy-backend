package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotification;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
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

    public void sendNotifications(Long chatId, ChatNotification notification, MessageCallback callback) {
        switch(notification.getType()) {
            case MESSAGE:
                sendNotificationConvertTimeZone((ChatNotificationMessage) notification, chatId, callback);
                break;
            case JOINED, LEFT:
                sendNotification(notification, chatId, callback);
                break;
        }
    }

    public void sendNotifications(Long chatId, ChatNotification notification) {
        sendNotification(notification, chatId, username -> {});
    }

    public void patchMetadata(Long chatId, String username, Map<String, Object> headers) {
        Optional<String> timeZone = HeaderUtils.getOptionalHeaderSingleValue(headers, TIMEZONE_HEADER_NAME, String.class);
        ChatUserMetadata metadata = chatSessionManager.get(chatId, username);
        timeZone.ifPresent(s -> metadata.setZoneId(TimeUtils.get(s)));
    }

    public void subscribeIfAbsent(Long chatId, String username, String timeZone) {
        ChatUserMetadata metadata = new ChatUserMetadata(username, TimeUtils.getOrSystemDefault(timeZone));
        chatSessionManager.putIfAbsent(chatId, metadata);
    }

    public void unsubscribeIfPresent(Long chatId, String username) {
        chatSessionManager.remove(chatId, username);
    }

    private void sendNotificationConvertTimeZone(ChatNotificationMessage notification, Long chatId, MessageCallback callback) {
        chatSessionManager.get(chatId).forEach(userMetadata -> {
            String username = userMetadata.getUsername();
            String destination = String.format(SUBSCRIPTION_URL_PATTERN, chatId, username);
            ChatMessageDTO messageDTOConverted = chatMapper.mapTimeZone(notification.getContent(), userMetadata.getZoneId());

            simpMessagingTemplate.convertAndSend(destination, messageDTOConverted);
            callback.onMessageSent(username);
        });
    }

    private void sendNotification(ChatNotification notification, Long chatId,  MessageCallback callback) {
        chatSessionManager.get(chatId).forEach(userMetadata -> {
            String username = userMetadata.getUsername();
            String destination = String.format(SUBSCRIPTION_URL_PATTERN, chatId, username);

            simpMessagingTemplate.convertAndSend(destination, notification);
            callback.onMessageSent(username);
        });
    }
}

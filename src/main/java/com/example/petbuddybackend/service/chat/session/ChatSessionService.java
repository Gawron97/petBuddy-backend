package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotification;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationType;
import com.example.petbuddybackend.service.chat.session.context.ChatSessionContext;
import com.example.petbuddybackend.service.mapper.ChatMapper;
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
        ChatRoomMetadata chatRoomMeta = chatSessionManager.get(chatId);

        chatRoomMeta.forEach(userMetadata -> {
            if(notification.getType().equals(ChatNotificationType.MESSAGE)) {
                convertNotificationMessageTimezone(notification, userMetadata.getZoneId());
            }

            executeSendNotification(notification, chatId, userMetadata.getUsername());
            callback.onMessageSent(userMetadata.getUsername());
        });
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
        sessionContext.setContext(chatId, username, chatSessionManager::remove);
        ChatUserMetadata metadata = new ChatUserMetadata(username, TimeUtils.getOrSystemDefault(timeZone));
        chatSessionManager.putIfAbsent(chatId, metadata);
    }

    public void unsubscribe(Long chatId, String username) {
        sessionContext.clearContext();
        chatSessionManager.remove(chatId, username);
    }

    private void executeSendNotification(ChatNotification notification, Long chatId, String receiverUsername) {
        simpMessagingTemplate.convertAndSend(formatDestination(chatId, receiverUsername), notification);
    }

    private String formatDestination(Long chatId, String username) {
        return String.format(SUBSCRIPTION_URL_PATTERN, chatId, username);
    }

    private void convertNotificationMessageTimezone(ChatNotification notification, ZoneId zoneId) {
        ChatNotificationMessage notificationMessage = (ChatNotificationMessage) notification;
        ChatMessageDTO mappedMessage = chatMapper.mapTimeZone(notificationMessage.getContent(), zoneId);
        notificationMessage.setContent(mappedMessage);
    }
}

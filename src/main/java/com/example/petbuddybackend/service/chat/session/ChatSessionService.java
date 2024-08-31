package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    @Value("${url.chat.topic.pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Value("${url.chat.topic.base}")
    private String SUBSCRIPTION_URL_BASE;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatSessionManager chatSessionManager;
    private final ChatMapper chatMapper = ChatMapper.INSTANCE;

    public void sendMessages(Long chatId, ChatMessageDTO messageDTO) {
        chatSessionManager.get(chatId).forEach(userMetadata ->
                simpMessagingTemplate.convertAndSend(
                        String.format(SUBSCRIPTION_URL_PATTERN, chatId, userMetadata.getUsername()),
                        chatMapper.mapTimeZone(messageDTO, userMetadata.getZoneId())
                ));
    }

    public void patchMetadata(Long chatId, String username, Map<String, Object> headers) {
        Optional<String> timeZone = HeaderUtils.getOptionalHeaderSingleValue(headers, TIMEZONE_HEADER_NAME, String.class);
        ChatUserMetadata metadata = chatSessionManager.get(chatId, username);
        timeZone.ifPresent(s -> metadata.setZoneId(TimeUtils.get(s)));
    }

    public void subscribeIfAbsent(Long chatId, String username, String timeZone) {
        chatSessionManager.computeIfAbsent(
                chatId,
                () -> new ChatUserMetadata(username, TimeUtils.getOrSystemDefault(timeZone))
        );
    }

    public void subscribeIfAbsent(StompHeaderAccessor accessor) {
        String username = accessor.getUser().getName();
        String destination = accessor.getDestination();
        String timeZone = accessor.getFirstNativeHeader(TIMEZONE_HEADER_NAME);

        if(destination != null && destination.startsWith(SUBSCRIPTION_URL_BASE)) {
            String[] parts = destination.split("/");
            if (parts.length > 3) {
                Long chatId = Long.parseLong(parts[3]);
                subscribeIfAbsent(chatId, username, timeZone);
            }
        }
    }

    public void unsubscribeIfPresent(Long chatId, String username) {
        chatSessionManager.removeIfPresent(chatId, username);
    }

    public void unsubscribeIfPresent(StompHeaderAccessor accessor) {
        String username = accessor.getUser().getName();
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith(SUBSCRIPTION_URL_BASE)) {
            String[] parts = destination.split("/");
            if (parts.length > 3) {
                Long chatId = Long.parseLong(parts[3]);
                unsubscribeIfPresent(chatId, username);
            }
        }
    }
}

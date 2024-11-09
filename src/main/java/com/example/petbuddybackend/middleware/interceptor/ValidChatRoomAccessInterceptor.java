package com.example.petbuddybackend.middleware.interceptor;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidChatRoomAccessInterceptor implements ChannelInterceptor {

    @Value("${url.chat.topic.subscribe-prefix}")
    private String URL_CHAT_TOPIC_BASE;

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    private final ChatService chatService;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String destination = accessor.getDestination();

        if(HeaderUtils.destinationStartsWith(URL_CHAT_TOPIC_BASE, destination)) {
            handleMessageTopic(accessor, destination);
        }

        return message;
    }

    private void handleMessageTopic(StompHeaderAccessor accessor, String destination) {
        StompCommand command = accessor.getCommand();
        Role role = Role.valueOf(accessor.getFirstNativeHeader(ROLE_HEADER_NAME));

        if(!StompCommand.SUBSCRIBE.equals(command) && !StompCommand.SEND.equals(command)) {
            return;
        }

        String username = HeaderUtils.getUser(accessor);
        Long chatId = extractChatId(destination);

        if (!chatService.isUserInChat(chatId, username, role)) {
            throw new NotParticipateException("Action not allowed for this user");
        }
    }

    private Long extractChatId(String destination) {
        String[] parts = destination.split("/");
        return Long.parseLong(parts[CHAT_ID_INDEX_IN_TOPIC_URL]);
    }
}

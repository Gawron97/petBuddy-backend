package com.example.petbuddybackend.filter;

import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class SubscriptionInterceptor implements ChannelInterceptor {

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal userPrincipal = accessor.getUser();
            if (userPrincipal != null) {
                String username = userPrincipal.getName();
                String destination = accessor.getDestination();
                Long chatId = extractChatId(destination);

                if (!usernameIsParticipatingChatRoom(username, chatId)) {
                    throw new NotParticipateException("Subscription not allowed for this user.");
                }
            }
        }

        return message;
    }

    private boolean usernameIsParticipatingChatRoom(String username, Long chatRoomId) {
        return chatService.isUserInChat(chatRoomId, username);
    }

    private Long extractChatId(String destination) {
        String[] parts = destination.split("/");
        return Long.parseLong(parts[3]);
    }
}

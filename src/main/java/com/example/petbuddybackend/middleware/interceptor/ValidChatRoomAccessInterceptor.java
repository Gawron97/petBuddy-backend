package com.example.petbuddybackend.middleware.interceptor;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidChatRoomAccessInterceptor implements ChannelInterceptor, ApplicationContextAware {

    private static final String CHAT = "Chat";
    private static final String USER_NOT_IN_CHAT_MESSAGE = "User %s is not in chat %d";

    @Value("${url.chat.topic.subscribe-prefix}")
    private String URL_CHAT_TOPIC_BASE;

    @Value("${url.exception.topic.send-url}")
    public String EXCEPTIONS_PATH;

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    private final ChatService chatService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private ApplicationContext applicationContext;

    /**
     * Required for lazy loading {@link SimpMessagingTemplate} as it is initialized after this interceptor
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Lazy load SimpMessagingTemplate
        if (simpMessagingTemplate == null) {
            simpMessagingTemplate = applicationContext.getBean(SimpMessagingTemplate.class);
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String destination = accessor.getDestination();

        if(HeaderUtils.destinationStartsWith(URL_CHAT_TOPIC_BASE, destination) && !accessPermitted(accessor, destination)) {
            return null;
        }

        return message;
    }

    private boolean accessPermitted(StompHeaderAccessor accessor, String destination) {
        StompCommand command = accessor.getCommand();
        Role role = Role.valueOf(accessor.getFirstNativeHeader(ROLE_HEADER_NAME));

        if(!StompCommand.SUBSCRIBE.equals(command) && !StompCommand.SEND.equals(command)) {
            return true;
        }

        String username = HeaderUtils.getUser(accessor);
        Long chatId = extractChatId(destination);
        String sessionId = accessor.getSessionId();

        return validateChatPermission(chatId, username, sessionId, role);
    }

    private boolean validateChatPermission(Long chatId, String username, String sessionId, Role role) {
        try {
            chatService.assertHasAccessToChatRoom(chatId, username, role);
        } catch(NotParticipateException | NotFoundException | BlockedException e) {
            log.debug("User {} is not allowed to access chat room {}", username, chatId);
            log.trace("Exception: {}", e.getMessage());
            log.trace("Sending exception message to user at destination {}", EXCEPTIONS_PATH);
            simpMessagingTemplate.convertAndSendToUser(
                    username,
                    EXCEPTIONS_PATH,
                    new ApiExceptionResponse(e, e.getMessage()),
                    HeaderUtils.createMessageHeadersWithSessionId(sessionId)
            );

            return false;
        }

        return true;
    }

    private Long extractChatId(String destination) {
        String[] parts = destination.split("/");
        return Long.parseLong(parts[CHAT_ID_INDEX_IN_TOPIC_URL]);
    }
}

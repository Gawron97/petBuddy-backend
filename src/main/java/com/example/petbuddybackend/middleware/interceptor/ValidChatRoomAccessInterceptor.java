package com.example.petbuddybackend.middleware.interceptor;

import com.example.petbuddybackend.dto.chat.notification.ChatNotificationBlock;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.block.BlockService;
import com.example.petbuddybackend.service.block.BlockType;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.exception.ApiExceptionResponse;
import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
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

    @Value("${url.chat.topic.subscribe-prefix}")
    private String URL_CHAT_TOPIC_BASE;

    @Value("${url.chat.topic.send-url}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Value("${url.exception.topic.send-url}")
    public String EXCEPTIONS_PATH;

    @Value("${url.chat.topic.chat-id-pos}")
    private int CHAT_ID_INDEX_IN_TOPIC_URL;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    private final ChatService chatService;
    private final BlockService blockService;
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

        if(!HeaderUtils.destinationStartsWith(URL_CHAT_TOPIC_BASE, destination)) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        Role role = Role.valueOf(accessor.getFirstNativeHeader(ROLE_HEADER_NAME));
        String username = HeaderUtils.getUser(accessor);
        Long chatId = extractChatId(destination);
        String sessionId = accessor.getSessionId();
        ChatRoom chatRoom = chatService.getChatRoomById(chatId);

        if(!accessPermitted(command, chatRoom, username, sessionId, role)) {
            return null;
        }

        return message;
    }

    private boolean accessPermitted(StompCommand command,
                                    ChatRoom chatRoom,
                                    String username,
                                    String sessionId,
                                    Role role) {
        if(StompCommand.SUBSCRIBE.equals(command)) {
            return validateUserParticipatesInChat(chatRoom, username, sessionId, role);
        }

        if(StompCommand.SEND.equals(command)) {
            return validateUserParticipatesInChat(chatRoom, username, sessionId, role) &&
                    validateUserNotBlocked(chatRoom, username, sessionId);
        }

        return true;
    }


    private boolean validateUserNotBlocked(ChatRoom chatRoom, String username, String sessionId) {
        if(blockService.isBlockedByAny(chatRoom.getCaretaker().getEmail(), chatRoom.getClient().getEmail())) {
            sendBlockNotificationToChatTopic(chatRoom.getId(), username, sessionId);
            return false;
        }

        return true;
    }

    private boolean validateUserParticipatesInChat(ChatRoom chatRoom, String username, String sessionId, Role role) {
        try {
            chatService.assertUserInChat(chatRoom, username, role);
            return true;
        } catch(NotParticipateException | NotFoundException e) {
            sendGeneralExceptionMessage(e, username, chatRoom.getId(), sessionId);
            return false;
        }
    }

    private void sendGeneralExceptionMessage(HttpException e, String username, Long chatId, String sessionId) {
        log.debug("User {} is not allowed to access chat room {}", username, chatId);
        log.trace("Exception: {}", e.getMessage());
        log.trace("Sending exception message to user at destination {}", EXCEPTIONS_PATH);
        simpMessagingTemplate.convertAndSendToUser(
                username,
                EXCEPTIONS_PATH,
                new ApiExceptionResponse(e, e.getMessage()),
                HeaderUtils.createMessageHeadersWithSessionId(sessionId)
        );
    }

    private void sendBlockNotificationToChatTopic(Long chatId, String username, String sessionId) {
        String destination = String.format(CHAT_TOPIC_URL_PATTERN, chatId);
        log.trace("Sending block message to user at destination {}", destination);
        simpMessagingTemplate.convertAndSendToUser(
                username,
                destination,
                new ChatNotificationBlock(chatId, BlockType.BLOCKED),
                HeaderUtils.createMessageHeadersWithSessionId(sessionId)
        );
    }

    private Long extractChatId(String destination) {
        String[] parts = destination.split("/");
        return Long.parseLong(parts[CHAT_ID_INDEX_IN_TOPIC_URL]);
    }
}

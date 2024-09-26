package com.example.petbuddybackend.middleware.interceptor;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ValidChatRoomAccessInterceptorTest {

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Value("${url.chat.topic.pattern}")
    private String URL_CHAT_TOPIC_PATTERN;

    @Autowired
    private ValidChatRoomAccessInterceptor interceptor;

    @MockBean
    private ChatService chatService;

    @Test
    public void testUserInChatAllowed() {
        String username = "user1";
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, username, role);

        when(chatService.isUserInChat(any(Long.class), anyString(), any(Role.class)))
                .thenReturn(true);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertNotNull(result);
    }

    @Test
    public void testUserInChatDenied() {
        String username = "user1";
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, username, role);

        when(chatService.isUserInChat(any(Long.class), anyString(), any(Role.class)))
                .thenReturn(false);

        assertThrows(NotParticipateException.class, () -> {
            interceptor.preSend(message, mock(MessageChannel.class));
        });
    }

    private Message<?> createMessage(String destination, String username, Role role) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination(destination);
        headerAccessor.setNativeHeader(ROLE_HEADER_NAME, role.name());
        headerAccessor.setUser(() -> username);

        return new GenericMessage<>(new byte[0], headerAccessor.getMessageHeaders());
    }
}

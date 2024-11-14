package com.example.petbuddybackend.middleware.interceptor;

import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.service.block.BlockService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ValidChatRoomAccessInterceptorTest {

    public static final String CLIENT_EMAIL = "ClientEmail";
    public static final String CARETAKER_EMAIL = "CaretakerEmail";
    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Value("${url.chat.topic.client-subscribe-pattern}")
    private String URL_CHAT_TOPIC_PATTERN;

    @Autowired
    private ValidChatRoomAccessInterceptor interceptor;

    @MockBean
    private ChatRoomRepository chatRoomRepository;

    @MockBean
    private BlockService blockService;

    ChatRoom chatRoom;

    @BeforeEach
    public void setUp() {
        chatRoom = ChatRoom.builder()
                .client(Client.builder().email(CLIENT_EMAIL).build())
                .caretaker(Caretaker.builder().email(CARETAKER_EMAIL).build())
                .build();
    }

    @Test
    public void testUserInChatAllowed_shouldNotThrow() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1);
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, CLIENT_EMAIL, role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        when(blockService.isBlockedByAny(any(), any()))
                .thenReturn(false);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNotNull(result);
    }

    @Test
    public void testUserSubscribeToChat_doesNotParticipate_shouldSendExceptionMessage() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, "someOtherUsername", role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        when(blockService.isBlockedByAny(any(), any()))
                .thenReturn(false);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNull(result);
    }

    @Test
    public void testUserSubscribeToChat_isBlocked_shouldSendExceptionMessage() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, CLIENT_EMAIL, role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        when(blockService.isBlockedByAny(any(), any()))
                .thenReturn(true);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNotNull(result);
    }

    @Test
    public void testUserSubscribeToChat_noSuchChatRoom_shouldSendExceptionMessage() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, CARETAKER_EMAIL, role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        when(blockService.isBlockedByAny(any(), any()))
                .thenReturn(true);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNull(result);
    }


    private Message<?> createMessage(String destination, String username, Role role) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination(destination);
        headerAccessor.setNativeHeader(ROLE_HEADER_NAME, role.name());
        headerAccessor.setUser(() -> username);

        return new GenericMessage<>(new byte[0], headerAccessor.getMessageHeaders());
    }
}

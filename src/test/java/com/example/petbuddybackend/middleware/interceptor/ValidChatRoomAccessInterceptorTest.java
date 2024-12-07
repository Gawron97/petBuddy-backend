package com.example.petbuddybackend.middleware.interceptor;

import com.example.petbuddybackend.dto.chat.notification.ChatNotificationBlock;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.service.block.BlockService;
import com.example.petbuddybackend.service.block.BlockType;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ValidChatRoomAccessInterceptorTest {

    public static final String CLIENT_EMAIL = "ClientEmail";
    public static final String CARETAKER_EMAIL = "CaretakerEmail";

    @Value("${url.notification.topic.send-url}")
    private String NOTIFICATION_BASE_URL;

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @Value("${url.chat.topic.client-subscribe-pattern}")
    private String URL_CHAT_TOPIC_PATTERN;

    @Autowired
    private ValidChatRoomAccessInterceptor interceptor;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private ChatRoomRepository chatRoomRepository;

    @MockBean
    private BlockService blockService;

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        chatRoom = ChatRoom.builder()
                .client(Client.builder().email(CLIENT_EMAIL).build())
                .caretaker(Caretaker.builder().email(CARETAKER_EMAIL).build())
                .build();
    }

    @Test
    void testUserInChatAllowed_shouldNotThrow() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1);
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, CLIENT_EMAIL, role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNotNull(result);
    }

    @Test
    void testUserSubscribeToChat_doesNotParticipate_shouldSendExceptionMessage() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, "someOtherUsername", role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNull(result);
    }

    @Test
    void testUserSubscribeToChat_noSuchChatRoom_shouldSendExceptionMessage() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, CARETAKER_EMAIL, role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNull(result);
    }

    @Test
    void testUserSubscribeToChat_isBlocked_shouldSendBlockNotification() {
        String destination = String.format(URL_CHAT_TOPIC_PATTERN, 1, "someSessionId");
        Role role = Role.CLIENT;
        Message<?> message = createMessage(destination, CLIENT_EMAIL, role);

        when(chatRoomRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(chatRoom));

        doThrow(new BlockedException("", ""))
                .when(blockService)
                .assertNotBlockedByAny(any(), any());

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
        assertNull(result);

        ArgumentCaptor<ChatNotificationBlock> captor = ArgumentCaptor.forClass(ChatNotificationBlock.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                any(String.class),
                eq(NOTIFICATION_BASE_URL),
                captor.capture(),
                any(Map.class)
        );

        ChatNotificationBlock notification = captor.getValue();
        assertNotNull(notification);
        assertEquals(BlockType.BLOCKED, notification.getBlockType());
        assertEquals(1L, notification.getChatId());
    }


    private Message<?> createMessage(String destination, String username, Role role) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination(destination);
        headerAccessor.setNativeHeader(ROLE_HEADER_NAME, role.name());
        headerAccessor.setUser(() -> username);

        return new GenericMessage<>(new byte[0], headerAccessor.getMessageHeaders());
    }
}

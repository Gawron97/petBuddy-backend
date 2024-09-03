package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ChatSessionServiceTest {

    @Value("${url.chat.topic.pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Value("${header-name.timezone}")
    private String TIMEZONE_HEADER_NAME;

    @Autowired
    private ChatSessionService chatSessionService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private ChatSessionManager chatSessionManager;

    @Mock
    private StompHeaderAccessor stompHeaderAccessor;

    private ChatMapper chatMapper = ChatMapper.INSTANCE;

    @Test
    void testSendMessages_shouldSendProperPayload() {
        Long chatId = 1L;
        ChatMessageDTO messageDTO = MockChatProvider.createMockChatMessageDTO();
        ChatRoomMetadata chatRoomMetadata = createChatUserMeta("fstUsername", "sndUsername");
        when(chatSessionManager.get(chatId)).thenReturn(chatRoomMetadata);

        chatSessionService.sendMessages(chatId, messageDTO);

        for (ChatUserMetadata userMetadata : chatRoomMetadata) {
            verify(simpMessagingTemplate, times(1)).convertAndSend(
                    String.format(SUBSCRIPTION_URL_PATTERN, chatId, userMetadata.getUsername()),
                    chatMapper.mapTimeZone(messageDTO, userMetadata.getZoneId())
            );
        }
    }

    @Test
    void testPatchMetadata_shouldUpdateZoneId() {
        Long chatId = 1L;
        String username = "testUser";
        String newTimeZone = "America/New_York";
        ZoneId newZoneId = ZoneId.of(newTimeZone);

        Map<String, Object> headers = Map.of("timezone", newTimeZone);
        ChatUserMetadata existingMetadata = new ChatUserMetadata(username, ZoneId.systemDefault());

        when(chatSessionManager.get(any(), any()))
                .thenReturn(existingMetadata);

        // Mock the static method
        try (MockedStatic<HeaderUtils> headerUtilsMockedStatic = Mockito.mockStatic(HeaderUtils.class)) {
            headerUtilsMockedStatic.when(() -> HeaderUtils.getOptionalHeaderSingleValue(any(), any(), any()))
                    .thenReturn(Optional.of(newTimeZone));

            // when
            chatSessionService.patchMetadata(chatId, username, headers);

            // then
            verify(chatSessionManager).get(chatId, username);
            assertEquals(newZoneId, existingMetadata.getZoneId());
        }
    }

    @Test
    void testSubscribeIfAbsent_shouldCreateUserMetadata() {
        // Arrange
        Long chatId = 1L;
        String username = "testUser";
        String timeZone = "America/New_York";
        ZoneId expectedZoneId = ZoneId.of(timeZone);

        // Act
        chatSessionService.subscribeIfAbsent(chatId, username, timeZone);

        // Assert
        verify(chatSessionManager).putIfAbsent(
                eq(chatId),
                argThat(meta -> username.equals(meta.getUsername()) && expectedZoneId.equals(meta.getZoneId()))
        );
    }

    @Test
    void testSubscribeIfAbsent_shouldSubscribeUser() {
        // Arrange
        String username = "testUser";
        String destination = String.format(SUBSCRIPTION_URL_PATTERN, 1L, username);
        String timeZone = "America/New_York";
        Long chatId = 1L;

        when(stompHeaderAccessor.getUser()).thenReturn(() -> username);
        when(stompHeaderAccessor.getDestination()).thenReturn(destination);
        when(stompHeaderAccessor.getFirstNativeHeader("timezone")).thenReturn(timeZone);

        // Act
        chatSessionService.subscribeIfAbsent(stompHeaderAccessor);

        // Assert
        verify(chatSessionManager, times(1)).putIfAbsent(
                eq(chatId),
                any()
        );
        verify(stompHeaderAccessor).getUser();
        verify(stompHeaderAccessor).getDestination();
        verify(stompHeaderAccessor).getFirstNativeHeader(TIMEZONE_HEADER_NAME);
    }

    @Test
    void testUnsubscribeIfPresent_shouldRemoveUserFromChatSession() {
        // given
        Long chatId = 1L;
        String username = "testUser";

        // when
        chatSessionService.unsubscribeIfPresent(chatId, username);

        // then
        verify(chatSessionManager).remove(chatId, username);
    }

    @Test
    void testUnsubscribeIfPresent_withStompHeaderAccessor_shouldRemoveUserFromChatSession() {
        // given
        String username = "testUser";
        String destination = "/topic/messages/1";
        Long chatId = 1L;

        when(stompHeaderAccessor.getUser()).thenReturn(() -> username);
        when(stompHeaderAccessor.getDestination()).thenReturn(destination);

        // when
        chatSessionService.unsubscribeIfPresent(stompHeaderAccessor);

        // then
        verify(stompHeaderAccessor).getUser();
        verify(stompHeaderAccessor).getDestination();
        verify(chatSessionManager).remove(chatId, username);
    }

    private ChatRoomMetadata createChatUserMeta(String firstUsername, String secondUsername) {
        return new ChatRoomMetadata(
                new ChatUserMetadata(firstUsername, ZoneId.systemDefault()),
                new ChatUserMetadata(secondUsername, ZoneId.systemDefault())
        );
    }
}

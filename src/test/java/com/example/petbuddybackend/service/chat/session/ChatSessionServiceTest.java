package com.example.petbuddybackend.service.chat.session;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationJoined;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationLeft;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationMessage;
import com.example.petbuddybackend.service.chat.session.context.WebSocketSessionContext;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import com.example.petbuddybackend.utils.time.TimeUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ChatSessionServiceTest {

    @Value("${url.chat.topic.pattern}")
    private String SUBSCRIPTION_URL_PATTERN;

    @Autowired
    private ChatSessionService chatSessionService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private WebSocketSessionContext mockContext;

    @MockBean
    private ChatSessionManager chatSessionManager;

    private ChatMapper chatMapper = ChatMapper.INSTANCE;

    @Test
    void testSendMessageNotification_shouldSendProperPayload() {
        Long chatId = 1L;
        ChatMessageDTO messageDTO = MockChatProvider.createMockChatMessageDTO();
        ChatRoomSessionMetadata chatRoomMetadata = createChatUserMeta("fstUsername", "sndUsername");
        when(chatSessionManager.find(chatId)).thenReturn(Optional.of(chatRoomMetadata));

        ChatNotificationMessage payload = new ChatNotificationMessage(messageDTO);
        chatSessionService.sendNotifications(chatId, payload, (empty) -> {});

        for (ChatUserMetadata userMetadata : chatRoomMetadata) {
            verify(simpMessagingTemplate, times(1)).convertAndSend(
                    String.format(SUBSCRIPTION_URL_PATTERN, chatId, userMetadata.getSessionId()),
                    new ChatNotificationMessage(chatMapper.mapTimeZone(messageDTO, userMetadata.getZoneId()))
            );
        }
    }

    @Test
    void testSendJoinNotification_shouldSendProperPayload() {
        Long chatId = 1L;
        ChatRoomSessionMetadata chatRoomMetadata = createChatUserMeta("fstUsername", "sndUsername");
        when(chatSessionManager.find(chatId)).thenReturn(Optional.of(chatRoomMetadata));

        ChatNotificationJoined payload = new ChatNotificationJoined(chatId, "fstUsername");
        chatSessionService.sendNotifications(chatId, payload);

        for (ChatUserMetadata userMetadata : chatRoomMetadata) {
            verify(simpMessagingTemplate, times(1)).convertAndSend(
                    String.format(SUBSCRIPTION_URL_PATTERN, chatId, userMetadata.getSessionId()),
                    payload
            );
        }
    }

    @Test
    void testSendLeftNotification_shouldSendProperPayload() {
        Long chatId = 1L;
        ChatRoomSessionMetadata chatRoomMetadata = createChatUserMeta("fstUsername", "sndUsername");
        when(chatSessionManager.find(chatId)).thenReturn(Optional.of(chatRoomMetadata));

        ChatNotificationLeft payload = new ChatNotificationLeft(chatId, "fstUsername");
        chatSessionService.sendNotifications(chatId, payload);

        for (ChatUserMetadata userMetadata : chatRoomMetadata) {
            verify(simpMessagingTemplate, times(1)).convertAndSend(
                    String.format(SUBSCRIPTION_URL_PATTERN, chatId, userMetadata.getSessionId()),
                    payload
            );
        }
    }

    @Test
    void testPatchMetadata_shouldUpdateZoneId() {
        Long chatId = 1L;
        String username = "testUser";
        String newTimeZone = "America/New_York";
        String sessionId = "sessionId";
        ZoneId newZoneId = ZoneId.of(newTimeZone);

        Map<String, Object> headers = Map.of("timezone", newTimeZone);
        ChatUserMetadata existingMetadata = new ChatUserMetadata(username, sessionId, ZoneId.systemDefault());

        when(chatSessionManager.find(any(), any(), any()))
                .thenReturn(Optional.of(existingMetadata));

        // Mock the static method
        try (MockedStatic<HeaderUtils> headerUtilsMockedStatic = Mockito.mockStatic(HeaderUtils.class)) {
            headerUtilsMockedStatic.when(() -> HeaderUtils.getOptionalNativeHeaderSingleValue(any(Map.class), any(), any()))
                    .thenReturn(Optional.of(newTimeZone));

            chatSessionService.patchMetadata(chatId, username, sessionId, headers);

            verify(chatSessionManager).find(chatId, username, sessionId);
            assertEquals(newZoneId, existingMetadata.getZoneId());
        }
    }

    @Test
    void testSubscribeIfAbsent_shouldCreateUserMetadata() {
        Long chatId = 1L;
        String username = "testUser";
        String timeZone = "America/New_York";
        String sessionId = "sessionId";
        ZoneId expectedZoneId = ZoneId.of(timeZone);

        chatSessionService.subscribe(chatId, username, sessionId, TimeUtils.getOrSystemDefault(timeZone), "1");

        verify(chatSessionManager).put(
                eq(chatId),
                argThat(meta -> username.equals(meta.getUsername()) && expectedZoneId.equals(meta.getZoneId()))
        );
    }

    @Test
    void testSubscribeIfAbsent_shouldSubscribeUser() {
        String username = "testUser";
        String timeZone = "America/New_York";
        Long chatId = 1L;
        String sessionId = "sessionId";

        chatSessionService.subscribe(chatId, username, sessionId, TimeUtils.getOrSystemDefault(timeZone), "1");

        verify(chatSessionManager, times(1))
                .put(eq(chatId), any());
    }

    private ChatRoomSessionMetadata createChatUserMeta(String firstUsername, String secondUsername) {
        return new ChatRoomSessionMetadata(
                new ChatUserMetadata(firstUsername, "fstSessionId", ZoneId.systemDefault()),
                new ChatUserMetadata(secondUsername, "sndSessionId", ZoneId.systemDefault())
        );
    }
}

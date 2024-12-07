package com.example.petbuddybackend.service.block.event;

import com.example.petbuddybackend.dto.chat.notification.ChatNotificationBlock;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.service.block.BlockType;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.WebSocketChatMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BlockEventServiceTest {

    private static final String BLOCKER_USERNAME = "user1";
    private static final String BLOCKED_USERNAME = "user2";

    private CareStateMachine careStateMachine;
    private ChatService chatService;
    private WebSocketChatMessageSender webSocketChatMessageSender;
    private BlockEventService blockEventService;

    @BeforeEach
    void setUp() {
        careStateMachine = mock(CareStateMachine.class);
        chatService = mock(ChatService.class);
        webSocketChatMessageSender = mock(WebSocketChatMessageSender.class);
        blockEventService = new BlockEventService(careStateMachine, chatService, webSocketChatMessageSender);
    }

    @Test
    void handleUserBlock_blockedType_shouldTriggerBlockLogic() {
        BlockEvent event = new BlockEvent(BLOCKER_USERNAME, BLOCKED_USERNAME, BlockType.BLOCKED);
        ChatRoom chatRoom = ChatRoom.builder().id(1L).build();

        when(chatService.findChatRoomByParticipants(BLOCKER_USERNAME, BLOCKED_USERNAME))
                .thenReturn(Optional.of(chatRoom));

        when(chatService.findChatRoomByParticipants(BLOCKED_USERNAME, BLOCKER_USERNAME))
                .thenReturn(Optional.of(chatRoom));

        blockEventService.handleUserBlock(event);

        verify(careStateMachine).cancelCaresIfStatePermitsAndSave(BLOCKER_USERNAME, BLOCKED_USERNAME);

        ArgumentCaptor<ChatNotificationBlock> captor = ArgumentCaptor.forClass(ChatNotificationBlock.class);
        verify(webSocketChatMessageSender, times(2)).sendMessages(eq(chatRoom), captor.capture());

        ChatNotificationBlock capturedNotification = captor.getValue();
        assertEquals(BlockType.BLOCKED, capturedNotification.getBlockType());
        assertEquals(1L, capturedNotification.getChatId());
    }
}

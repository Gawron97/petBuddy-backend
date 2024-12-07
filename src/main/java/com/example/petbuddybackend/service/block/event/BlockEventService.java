package com.example.petbuddybackend.service.block.event;

import com.example.petbuddybackend.service.block.BlockType;
import com.example.petbuddybackend.dto.chat.notification.ChatNotificationBlock;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.chat.WebSocketChatMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockEventService {

    private final CareStateMachine careStateMachine;
    private final ChatService chatService;
    private final WebSocketChatMessageSender webSocketChatMessageSender;

    @EventListener
    public void handleUserBlock(BlockEvent event) {
        switch(event.blockType()) {
            case BLOCKED -> onUserBlock(event.blockerUsername(), event.blockedUsername());
            case UNBLOCKED -> onUserUnblock(event.blockerUsername(), event.blockedUsername());
            default -> throw new UnsupportedOperationException("Unsupported block type");
        }
    }

    private void onUserBlock(String blockerUsername, String blockedUsername) {
        sendMessagesOnBlockEvent(blockerUsername, blockedUsername, BlockType.BLOCKED);
        careStateMachine.cancelCaresIfStatePermitsAndSave(blockerUsername, blockedUsername);
    }

    private void onUserUnblock(String blockerUsername, String blockedUsername) {
        sendMessagesOnBlockEvent(blockerUsername, blockedUsername, BlockType.UNBLOCKED);
    }

    // TODO: test "ifPresent"
    /**
     * Sends block message to chat room of users. Users can have two chats but as different role for example:
     * user1: client - user2: caretaker
     * and other chat
     * user1: caretaker - user2: client
     * */
    private void sendMessagesOnBlockEvent(String firstUsername, String secondUsername, BlockType blockType) {
        chatService.findChatRoomByParticipants(firstUsername, secondUsername)
                .ifPresent(chatRoom -> webSocketChatMessageSender.sendMessages(
                        chatRoom,
                        new ChatNotificationBlock(chatRoom.getId(), blockType))
                );

        chatService.findChatRoomByParticipants(secondUsername, firstUsername)
                .ifPresent(chatRoom -> webSocketChatMessageSender.sendMessages(
                        chatRoom,
                        new ChatNotificationBlock(chatRoom.getId(), blockType))
                );
    }
}

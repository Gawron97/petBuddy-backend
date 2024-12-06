package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationBlock extends ChatNotification {

    private Long chatId;
    private BlockType blockType;

    private ChatNotificationBlock(BlockType blockType) {
        super(ChatNotificationType.BLOCK);
        this.blockType = blockType;
    }

    public ChatNotificationBlock(Long chatId, BlockType blockType) {
        this(blockType);
        this.chatId = chatId;
    }
}

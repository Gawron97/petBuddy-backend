package com.example.petbuddybackend.dto.chat.notification;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationMessage extends ChatNotification {

    private ChatMessageDTO content;

    public ChatNotificationMessage() {
        super(ChatNotificationType.SEND);
    }

    public ChatNotificationMessage(ChatMessageDTO chatMessage) {
        this();
        this.content = chatMessage;
    }
}


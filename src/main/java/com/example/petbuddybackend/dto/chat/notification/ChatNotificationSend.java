package com.example.petbuddybackend.dto.chat.notification;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationSend extends ChatNotification {

    private ChatMessageDTO content;

    private ChatNotificationSend() {
        super(ChatNotificationType.SEND);
    }

    public ChatNotificationSend(ChatMessageDTO chatMessage) {
        this();
        this.content = chatMessage;
    }
}


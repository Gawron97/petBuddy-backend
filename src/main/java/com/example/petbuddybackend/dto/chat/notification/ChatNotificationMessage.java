package com.example.petbuddybackend.dto.chat.notification;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChatNotificationMessage extends ChatNotification {

    private ChatMessageDTO content;

    public ChatNotificationMessage(ChatMessageDTO chatMessage) {
        super(ChatNotificationType.MESSAGE);
        this.content = chatMessage;
    }
}


package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationJoined extends ChatNotification {

    private Long chatId;
    private String joiningUserEmail;

    public ChatNotificationJoined(Long chatId, String userEmail) {
        super(ChatNotificationType.JOINED);
        this.chatId = chatId;
        this.joiningUserEmail = userEmail;
    }
}

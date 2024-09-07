package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationLeft extends ChatNotification {

    private Long chatId;
    private String leavingUserEmail;

    public ChatNotificationLeft(Long chatId, String userEmail) {
        super(ChatNotificationType.LEFT);
        this.chatId = chatId;
        this.leavingUserEmail = userEmail;
    }
}

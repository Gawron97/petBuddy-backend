package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationLeave extends ChatNotification {

    private Long chatId;
    private String leavingUserEmail;

    private ChatNotificationLeave() {
        super(ChatNotificationType.LEAVE);
    }

    public ChatNotificationLeave(Long chatId, String userEmail) {
        this();
        this.chatId = chatId;
        this.leavingUserEmail = userEmail;
    }
}

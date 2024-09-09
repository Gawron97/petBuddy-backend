package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChatNotificationLeft extends ChatNotification {

    private Long chatId;
    private String leavingUserEmail;

    public ChatNotificationLeft(Long chatId, String userEmail) {
        super(ChatNotificationType.LEFT);
        this.chatId = chatId;
        this.leavingUserEmail = userEmail;
    }
}

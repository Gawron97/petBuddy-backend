package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChatNotificationJoined extends ChatNotification {

    private Long chatId;
    private String joiningUserEmail;

    public ChatNotificationJoined(Long chatId, String userEmail) {
        super(ChatNotificationType.JOINED);
        this.chatId = chatId;
        this.joiningUserEmail = userEmail;
    }
}

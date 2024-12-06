package com.example.petbuddybackend.dto.chat.notification;

import com.example.petbuddybackend.dto.notification.NotificationType;
import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.dto.notification.UnseenChatsNotificationDTO;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationJoin extends ChatNotification {

    private Long chatId;
    private String joiningUserEmail;

    private ChatNotificationJoin() {
        super(ChatNotificationType.JOIN);
    }

    public ChatNotificationJoin(Long chatId, String userEmail) {
        this();
        this.chatId = chatId;
        this.joiningUserEmail = userEmail;
    }
}

package com.example.petbuddybackend.dto.chat.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatNotificationConnected extends ChatNotification {

    private String sessionId;
    private String connectingUserEmail;

    public ChatNotificationConnected() {
        super(ChatNotificationType.CONNECT);
    }

    public ChatNotificationConnected(String sessionId, String username) {
        this();
        this.sessionId = sessionId;
        this.connectingUserEmail = username;
    }
}

package com.example.petbuddybackend.dto.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    SIMPLE_NOTIFICATION("SIMPLE_NOTIFICATION"), CHAT_NOTIFICATION("CHAT_NOTIFICATION");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public static final String SIMPLE_NOTIFICATION_VALUE = "SIMPLE_NOTIFICATION";
    public static final String CHAT_NOTIFICATION_VALUE = "CHAT_NOTIFICATION";

}

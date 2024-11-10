package com.example.petbuddybackend.dto.notification;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public final class UnseenChatsNotificationDTO extends NotificationDTO {
    private final String dType = "ChatNotification";
    private final int unseenChats;
}

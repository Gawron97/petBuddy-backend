package com.example.petbuddybackend.dto.notification;

import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Role;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder
@Getter
public final class SimplyNotificationDTO extends NotificationDTO {
    private final String dType = "Notification";
    private final Long notificationId;
    private final Long objectId;
    private final ObjectType objectType;
    private final String messageKey;
    private final Set<String> args;
    private final Role receiverProfile;
    private final boolean isRead;
}

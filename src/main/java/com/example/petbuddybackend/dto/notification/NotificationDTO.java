package com.example.petbuddybackend.dto.notification;

import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Role;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.Set;

@Builder
public record NotificationDTO(
        Long notificationId,
        Long objectId,
        ObjectType objectType,
        ZonedDateTime createdAt,
        String messageKey,
        Set<String> args,
        Role receiverProfile,
        boolean isRead
) {
}

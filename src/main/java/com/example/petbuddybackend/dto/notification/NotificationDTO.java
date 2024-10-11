package com.example.petbuddybackend.dto.notification;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record NotificationDTO(
        Long notificationId,
        Long objectId,
        String objectType,
        ZonedDateTime createdAt,
        String message,
        boolean isRead
) {
}

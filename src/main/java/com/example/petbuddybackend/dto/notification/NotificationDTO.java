package com.example.petbuddybackend.dto.notification;

import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Builder
@Getter @Setter
public class NotificationDTO {
    private Long notificationId;
    private Long objectId;
    private ObjectType objectType;
    private ZonedDateTime createdAt;
    private String message;
    private Role receiverProfile;
    private boolean isRead;
}

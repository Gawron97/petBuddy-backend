package com.example.petbuddybackend.dto.notification;

import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Role;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimplyNotificationDTO extends NotificationDTO {
    @Builder.Default
    private String dType = "Notification";
    private Long notificationId;
    private Long objectId;
    private  ObjectType objectType;
    private String messageKey;
    private Set<String> args;
    private Role receiverProfile;
    private boolean isRead;
}

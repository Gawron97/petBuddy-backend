package com.example.petbuddybackend.dto.notification;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnseenChatsNotificationDTO extends NotificationDTO {
    @Builder.Default
    private NotificationType dType = NotificationType.CHAT_NOTIFICATION;
    private int unseenChats;
}

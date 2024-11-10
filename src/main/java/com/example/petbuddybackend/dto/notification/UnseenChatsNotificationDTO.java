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
    private String dType = "ChatNotification";
    private int unseenChats;
}

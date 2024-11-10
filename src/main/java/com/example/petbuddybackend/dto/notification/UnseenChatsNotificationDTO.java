package com.example.petbuddybackend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnseenChatsNotificationDTO extends NotificationDTO {
    private String dType = "ChatNotification";
    private int unseenChats;
}

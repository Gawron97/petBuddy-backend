package com.example.petbuddybackend.dto.notification;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UnseenChatsNotificationDTO extends NotificationDTO {
    private int unseenChatsAsClient;
    private int unseenChatsAsCaretaker;
}

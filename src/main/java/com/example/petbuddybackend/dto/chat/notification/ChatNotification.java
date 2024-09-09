package com.example.petbuddybackend.dto.chat.notification;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class ChatNotification {
    private ChatNotificationType type;
}

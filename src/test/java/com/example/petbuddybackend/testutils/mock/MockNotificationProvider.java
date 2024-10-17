package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockNotificationProvider {

    public static CaretakerNotification createMockCaretakerNotification(Caretaker caretaker) {
        return CaretakerNotification.builder()
                .objectId(1L)
                .objectType(ObjectType.CARE)
                .message("Test notification message")
                .createdAt(ZonedDateTime.now())
                .isRead(false)
                .caretaker(caretaker)
                .build();
    }

    public static ClientNotification createMockClientNotification(Client client) {
        return ClientNotification.builder()
                .objectId(1L)
                .objectType(ObjectType.CARE)
                .message("Test notification message")
                .createdAt(ZonedDateTime.now())
                .isRead(false)
                .client(client)
                .build();
    }

}

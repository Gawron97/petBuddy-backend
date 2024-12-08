package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockNotificationProvider {

    public static CaretakerNotification createMockCaretakerNotification(Caretaker caretaker, Client client) {
        return CaretakerNotification.builder()
                .id(1L)
                .objectId(1L)
                .objectType(ObjectType.CARE)
                .messageKey("care_reservation")
                .args(Set.of("clientEmail"))
                .createdAt(ZonedDateTime.now())
                .read(false)
                .caretaker(caretaker)
                .clientTrigger(client)
                .build();
    }

    public static CaretakerNotification createMockCaretakerNotification(Caretaker caretaker, Client client, Long id) {
        return CaretakerNotification.builder()
                                    .id(id)
                                    .objectId(1L)
                                    .objectType(ObjectType.CARE)
                                    .messageKey("care_reservation")
                                    .args(Set.of("clientEmail"))
                                    .createdAt(ZonedDateTime.now())
                                    .read(false)
                                    .caretaker(caretaker)
                                    .clientTrigger(client)
                                    .build();
    }

    public static ClientNotification createMockClientNotification(Client client, Caretaker caretaker) {
        return ClientNotification.builder()
                .objectId(1L)
                .objectType(ObjectType.CARE)
                .messageKey("care_update")
                .args(Set.of("caretakerEmail"))
                .createdAt(ZonedDateTime.now())
                .read(false)
                .client(client)
                .caretakerTriggered(caretaker)
                .build();
    }

}

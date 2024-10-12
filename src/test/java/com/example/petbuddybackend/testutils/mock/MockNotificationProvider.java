package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockNotificationProvider {

    public static CaretakerNotification createMockCaretakerNotification(Caretaker caretaker) {
        return CaretakerNotification.builder()
                .objectId(1L)
                .objectType(ObjectType.CARE)
                .message("Test notification message")
                .caretaker(caretaker)
                .build();
    }

}

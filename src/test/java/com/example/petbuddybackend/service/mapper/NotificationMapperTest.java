package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.testutils.ValidationUtils;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static com.example.petbuddybackend.testutils.mock.MockNotificationProvider.createMockCaretakerNotification;
import static com.example.petbuddybackend.testutils.mock.MockNotificationProvider.createMockClientNotification;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationMapperTest {

    private final NotificationMapper mapper = NotificationMapper.INSTANCE;

    @Test
    void mapToNotificationDTO_whenNotificationForCaretaker_shouldNotLeaveNullFields() {

        //Given
        CaretakerNotification notification = createMockCaretakerNotification(createMockCaretaker());
        notification.setId(1L);

        NotificationDTO notificationDTO = mapper.mapToNotificationDTO(notification, ZoneId.systemDefault());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(notificationDTO));

    }

    @Test
    void mapToNotificationDTO_whenNotificationForClient_shouldNotLeaveNullFields() {

        //Given
        ClientNotification notification = createMockClientNotification(createMockClient());
        notification.setId(1L);

        NotificationDTO notificationDTO = mapper.mapToNotificationDTO(notification, ZoneId.systemDefault());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(notificationDTO));

    }

}
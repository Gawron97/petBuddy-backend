package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.service.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WebsocketNotificationService websocketNotificationService;
    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;


    public void addNotificationForCaretakerAndSend(Long objectId, String objectType, Caretaker caretaker, String message) {
        CaretakerNotification notification = createCaretakerNotification(objectId, objectType, caretaker, message);
        websocketNotificationService.sendNotification(
                caretaker.getEmail(),
                notificationMapper.mapToNotificationDTO(notification)
        );
    }

    public void addNotificationForClientAndSend(Long objectId, String objectType, Client client, String message) {
        ClientNotification notification = createClientNotification(objectId, objectType, client, message);
        websocketNotificationService.sendNotification(
                client.getEmail(),
                notificationMapper.mapToNotificationDTO(notification)
        );
    }

    private CaretakerNotification createCaretakerNotification(Long objectId, String objectType, Caretaker caretaker, String message) {
        return CaretakerNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .message(message)
                .caretaker(caretaker)
                .build();
    }

    private ClientNotification createClientNotification(Long objectId, String objectType, Client client, String message) {
        return ClientNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .message(message)
                .client(client)
                .build();
    }

}

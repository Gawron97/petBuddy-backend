package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.notification.ClientNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CaretakerNotificationRepository caretakerNotificationRepository;
    private final ClientNotificationRepository clientNotificationRepository;
    private final WebsocketNotificationService websocketNotificationService;


    public void addNotificationForCaretakerAndSend(Long objectId, ObjectType objectType, Caretaker caretaker, String message) {
        CaretakerNotification notification = createCaretakerNotification(objectId, objectType, caretaker, message);
        CaretakerNotification savedNotification = caretakerNotificationRepository.save(notification);
        websocketNotificationService.sendNotification(
                caretaker.getEmail(),
                savedNotification
        );
    }

    public void addNotificationForClientAndSend(Long objectId, ObjectType objectType, Client client, String message) {
        ClientNotification notification = createClientNotification(objectId, objectType, client, message);
        ClientNotification savedNotification = clientNotificationRepository.save(notification);
        websocketNotificationService.sendNotification(
                client.getEmail(),
                savedNotification
        );
    }

    private CaretakerNotification createCaretakerNotification(Long objectId, ObjectType objectType, Caretaker caretaker, String message) {
        return CaretakerNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .message(message)
                .caretaker(caretaker)
                .build();
    }

    private ClientNotification createClientNotification(Long objectId, ObjectType objectType, Client client, String message) {
        return ClientNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .message(message)
                .client(client)
                .build();
    }

}

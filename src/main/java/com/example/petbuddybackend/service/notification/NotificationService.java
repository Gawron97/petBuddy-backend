package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.notification.ClientNotificationRepository;
import com.example.petbuddybackend.service.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final CaretakerNotificationRepository caretakerNotificationRepository;
    private final ClientNotificationRepository clientNotificationRepository;
    private final WebsocketNotificationService websocketNotificationService;

    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;

    public void addNotificationForCaretakerAndSend(Long objectId, ObjectType objectType, Caretaker caretaker,
                                                   String messageKey, Set<String> args) {
        CaretakerNotification notification = createCaretakerNotification(objectId, objectType, caretaker,
                messageKey, args);
        CaretakerNotification savedNotification = caretakerNotificationRepository.save(notification);
        websocketNotificationService.sendNotification(
                caretaker.getEmail(),
                savedNotification
        );
    }

    public void addNotificationForClientAndSend(Long objectId, ObjectType objectType, Client client,
                                                String messageKey, Set<String> args) {
        ClientNotification notification = createClientNotification(objectId, objectType, client, messageKey, args);
        ClientNotification savedNotification = clientNotificationRepository.save(notification);
        websocketNotificationService.sendNotification(
                client.getEmail(),
                savedNotification
        );
    }

    public Page<NotificationDTO> getUnreadNotifications(Pageable pageable, String userEmail, Role userRole,
                                                        ZoneId timezone) {
        return userRole == Role.CARETAKER
                ? getCaretakerUnreadNotifications(pageable, userEmail, timezone)
                : getClientUnreadNotifications(pageable, userEmail, timezone);
    }

    private Page<NotificationDTO> getCaretakerUnreadNotifications(Pageable pageable, String caretakerEmail,
                                                                  ZoneId timezone) {
        return caretakerNotificationRepository.getCaretakerNotificationByCaretaker_EmailAndIsRead(
                caretakerEmail,
                false,
                pageable
                )
                .map(caretakerNotification -> notificationMapper.mapToNotificationDTO(caretakerNotification, timezone));
    }

    private Page<NotificationDTO> getClientUnreadNotifications(Pageable pageable, String clientEmail,
                                                               ZoneId timezone) {
        return clientNotificationRepository.getClientNotificationByClient_EmailAndIsRead(
                clientEmail,
                false,
                pageable
                )
                .map(clientNotification -> notificationMapper.mapToNotificationDTO(clientNotification, timezone));
    }

    private CaretakerNotification createCaretakerNotification(Long objectId, ObjectType objectType, Caretaker caretaker,
                                                              String messageKey, Set<String> args) {
        return CaretakerNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .messageKey(messageKey)
                .args(args)
                .caretaker(caretaker)
                .build();
    }

    private ClientNotification createClientNotification(Long objectId, ObjectType objectType, Client client,
                                                        String messageKey, Set<String> args) {
        return ClientNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .messageKey(messageKey)
                .args(args)
                .client(client)
                .build();
    }

}

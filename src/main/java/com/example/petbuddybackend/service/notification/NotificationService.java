package com.example.petbuddybackend.service.notification;

import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.Notification;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.notification.ClientNotificationRepository;
import com.example.petbuddybackend.repository.notification.NotificationRepository;
import com.example.petbuddybackend.service.mapper.NotificationMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final static String CARETAKER_NOTIFICATION = "Caretaker notification";
    private final static String CLIENT_NOTIFICATION = "Client notification";

    private final CaretakerNotificationRepository caretakerNotificationRepository;
    private final ClientNotificationRepository clientNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final WebsocketNotificationSender websocketNotificationSender;

    private final NotificationMapper notificationMapper = NotificationMapper.INSTANCE;

    public void addNotificationForCaretakerAndSend(Long objectId, ObjectType objectType, Caretaker caretaker,
                                                   Client triggeredBy, String messageKey, Set<String> args) {
        CaretakerNotification notification = createCaretakerNotification(objectId, objectType, caretaker, triggeredBy,
                messageKey, args);
        CaretakerNotification savedNotification = caretakerNotificationRepository.save(notification);
        websocketNotificationSender.sendNotification(
                caretaker.getEmail(),
                notificationMapper.mapToSimplyNotificationDTO(savedNotification)
        );
    }

    public void addNotificationForClientAndSend(Long objectId, ObjectType objectType, Client client,
                                                Caretaker triggeredBy, String messageKey, Set<String> args) {
        ClientNotification notification = createClientNotification(objectId, objectType, client, triggeredBy,
                messageKey, args);
        ClientNotification savedNotification = clientNotificationRepository.save(notification);
        websocketNotificationSender.sendNotification(
                client.getEmail(),
                notificationMapper.mapToSimplyNotificationDTO(savedNotification)
        );
    }

    public Page<SimplyNotificationDTO> getUnreadNotifications(Pageable pageable, String userEmail, Role userRole,
                                                              ZoneId timezone) {
        return userRole == Role.CARETAKER
                ? getCaretakerUnreadNotifications(pageable, userEmail, timezone)
                : getClientUnreadNotifications(pageable, userEmail, timezone);
    }

    public SimplyNotificationDTO markNotificationAsRead(Long notificationId, Role role, ZoneId timezone) {
        Notification notification = getNotification(role, notificationId);

        notification.setRead(true);
        return notificationMapper.mapToSimplyNotificationDTO(notificationRepository.save(notification), timezone);
    }

    public void markNotificationsAsRead(String username, Role role) {
        if(role == Role.CARETAKER) {
            caretakerNotificationRepository.markAllNotificationsOfCaretakerAsRead(username);
        } else {
            clientNotificationRepository.markAllNotificationsOfClientAsRead(username);
        }
    }

    private Notification getNotification(Role role, Long id) {
        return role == Role.CARETAKER
                ? caretakerNotificationRepository.findById(id)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER_NOTIFICATION, id.toString()))
                : clientNotificationRepository.findById(id)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CLIENT_NOTIFICATION, id.toString()));
    }

    private Page<SimplyNotificationDTO> getCaretakerUnreadNotifications(Pageable pageable, String caretakerEmail,
                                                                        ZoneId timezone) {
        return caretakerNotificationRepository.getCaretakerNotificationByCaretaker_EmailAndIsRead(
                caretakerEmail,
                false,
                pageable
                )
                .map(caretakerNotification ->
                        notificationMapper.mapToSimplyNotificationDTO(caretakerNotification, timezone));
    }

    private Page<SimplyNotificationDTO> getClientUnreadNotifications(Pageable pageable, String clientEmail,
                                                                     ZoneId timezone) {
        return clientNotificationRepository.getClientNotificationByClient_EmailAndIsRead(
                clientEmail,
                false,
                pageable
                )
                .map(clientNotification -> notificationMapper.mapToSimplyNotificationDTO(clientNotification, timezone));
    }

    private CaretakerNotification createCaretakerNotification(Long objectId, ObjectType objectType, Caretaker caretaker,
                                                              Client triggeredBy, String messageKey, Set<String> args) {
        return CaretakerNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .messageKey(messageKey)
                .args(args)
                .caretaker(caretaker)
                .triggeredBy(triggeredBy)
                .build();
    }

    private ClientNotification createClientNotification(Long objectId, ObjectType objectType, Client client,
                                                        Caretaker triggeredBy, String messageKey, Set<String> args) {
        return ClientNotification.builder()
                .objectId(objectId)
                .objectType(objectType)
                .messageKey(messageKey)
                .args(args)
                .client(client)
                .triggeredBy(triggeredBy)
                .build();
    }
}

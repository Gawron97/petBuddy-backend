package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.notification.SimplyNotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.entity.notification.Notification;
import com.example.petbuddybackend.entity.user.Role;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mapping(target = "notificationId", source = "id")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "mapToZonedDateTime")
    @Mapping(target = "receiverProfile", source = "notification", qualifiedByName = "mapToReceiverProfile")
    SimplyNotificationDTO mapToSimplyNotificationDTO(Notification notification, @Context ZoneId zoneId);

    @Mapping(target = "notificationId", source = "id")
    @Mapping(target = "receiverProfile", source = "notification", qualifiedByName = "mapToReceiverProfile")
    SimplyNotificationDTO mapToSimplyNotificationDTO(Notification notification);

    @Named("mapToZonedDateTime")
    default ZonedDateTime mapToZonedDateTime(ZonedDateTime date, @Context ZoneId zoneId) {
        return date.withZoneSameInstant(zoneId);
    }

    @Named("mapToReceiverProfile")
    default Role mapToReceiverProfile(Notification notification) {
        if(notification instanceof ClientNotification) {
            return Role.CLIENT;
        } else if(notification instanceof CaretakerNotification) {
            return Role.CARETAKER;
        } else {
            throw new IllegalStateException("Unknown receiver profile type");
        }
    }

}

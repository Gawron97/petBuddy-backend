package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.notification.NotificationDTO;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.notification.ClientNotification;
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
    NotificationDTO mapToNotificationDTO(CaretakerNotification notification);

    @Mapping(target = "notificationId", source = "id")
    NotificationDTO mapToNotificationDTO(ClientNotification notification);

    @Named("mapToZonedDateTime")
    default ZonedDateTime mapToZonedDateTime(ZonedDateTime date, @Context ZoneId zoneId) {
        return date.withZoneSameInstant(zoneId);
    }

}

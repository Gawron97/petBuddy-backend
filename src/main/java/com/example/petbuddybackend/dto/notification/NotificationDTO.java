package com.example.petbuddybackend.dto.notification;

import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimplyNotificationDTO.class, name = NotificationType.SIMPLE_NOTIFICATION_VALUE),
        @JsonSubTypes.Type(value = UnseenChatsNotificationDTO.class, name = NotificationType.CHAT_NOTIFICATION_VALUE)
})
@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    @JsonFormat(pattern = TimeUtils.ZONED_DATETIME_FORMAT)
    private ZonedDateTime createdAt;
}

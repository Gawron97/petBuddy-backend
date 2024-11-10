package com.example.petbuddybackend.dto.notification;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@SuperBuilder
@Setter
@Getter
public class NotificationDTO {
    private ZonedDateTime createdAt;
}

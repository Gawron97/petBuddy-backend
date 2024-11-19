package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.notification.ObjectType;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.notification.NotificationService;
import com.example.petbuddybackend.service.notification.WebsocketNotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CareScheduled {

    private final CareStateMachine careStateMachine;
    private final NotificationService notificationService;
    private final CareService careService;

    @Value("${notification.care.confirm}")
    private String confirmNeededMessage;

    @Scheduled(cron = "0 1 0 * * *")
    public void terminateCares() {
        int obsoletedCaresCount = careStateMachine.outdateCaresIfStatePermitsAndSave();
        log.info("Terminated cares from potential: {}", obsoletedCaresCount);
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void sendNotificationForConfirmCares() {
        careService.sendNotificationForConfirmCares();
    }

}

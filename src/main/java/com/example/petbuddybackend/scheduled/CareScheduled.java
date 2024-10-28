package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.service.care.state.CareStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CareScheduled {

    private final CareStateMachine careStateMachine;

    @Scheduled(cron = "0 1 0 * * *")
    public void terminateCares() {
        int obsoletedCaresCount = careStateMachine.outdateCaresIfStatePermitsAndSave();
        log.info("Terminated cares from potential: {}", obsoletedCaresCount);
    }
}

package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CareScheduled {

    private final CareRepository careRepository;
    private final CareStateMachine careStateMachine;

    @Scheduled(cron = "0 1 0 * * *")
    public void terminateCares() {
        List<CareStatus> obsoleteStatuses = List.of(CareStatus.CANCELLED, CareStatus.OUTDATED, CareStatus.PAID);

        List<Care> obsoleteCares = careRepository
                .findAllByCaretakerStatusNotInOrClientStatusNotIn(obsoleteStatuses, obsoleteStatuses);

        log.info("Terminating cares from potential: {}", obsoleteCares.size());
        obsoleteCares.forEach(care -> careStateMachine.transitionBothRoles(care, CareStatus.OUTDATED));
        careRepository.saveAll(obsoleteCares);
    }
}

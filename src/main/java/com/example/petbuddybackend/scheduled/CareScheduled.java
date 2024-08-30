package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.repository.care.CareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CareScheduled {

    private final CareRepository careRepository;

    @Scheduled(cron = "0 1 0 * * *")
    public void terminateCares() {

        List<Care> cares = careRepository.findAllByCaretakerStatusNotInOrClientStatusNotIn(
                List.of(CareStatus.CANCELLED, CareStatus.OUTDATED, CareStatus.PAID),
                List.of(CareStatus.CANCELLED, CareStatus.OUTDATED, CareStatus.PAID));
        log.info(MessageFormat.format("Terminating cares from potential: {0}", cares.size()));
        cares.forEach(care -> {
            if(care.getCareStart().isAfter(LocalDate.now()) || care.getCareStart().isEqual(LocalDate.now())) {
                care.setCaretakerStatus(CareStatus.OUTDATED);
                care.setClientStatus(CareStatus.OUTDATED);
            }
            careRepository.save(care);
        });

    }

}

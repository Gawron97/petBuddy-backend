package com.example.petbuddybackend.config;

import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.service.mock.MockService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class MockDataCreator {

    private static final int CARETAKER_COUNT = 50;

    private final CaretakerRepository caretakerRepository;
    private final AppUserRepository appUserRepository;
    private final MockService mockService;

    @PostConstruct
    public void createMockData() {
        if (shouldSkipInit()) {
            return;
        }
        log.info("Creating mock data in database...");

        var appUsers = appUserRepository.saveAllAndFlush(mockService.createAppUsers(CARETAKER_COUNT));
        caretakerRepository.saveAllAndFlush(mockService.createMockCaretakers(appUsers));

        log.info("Mock data created successfully!");
    }

    private boolean shouldSkipInit() {
        return caretakerRepository.count() != 0 &&
                appUserRepository.count() != 0;

    }
}

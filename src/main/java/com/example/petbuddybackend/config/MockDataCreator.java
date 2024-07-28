package com.example.petbuddybackend.config;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.repository.ClientRepository;
import com.example.petbuddybackend.repository.RatingRepository;
import com.example.petbuddybackend.service.mock.MockService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class MockDataCreator {

    private static final int CARETAKER_COUNT = 50;
    private static final int CLIENT_COUNT = 50;

    private final MockService mockService;
    private final CaretakerRepository caretakerRepository;
    private final ClientRepository clientRepository;
    private final AppUserRepository appUserRepository;
    private final RatingRepository ratingRepository;

    @PostConstruct
    public void createMockData() {
        if (shouldSkipInit()) {
            return;
        }
        log.info("Creating mock data in database...");

        List<AppUser> caretakerAppUsers = appUserRepository.saveAllAndFlush(mockService.createMockAppUsers(CARETAKER_COUNT));
        List<AppUser> clientAppUsers = appUserRepository.saveAllAndFlush(mockService.createMockAppUsers(CLIENT_COUNT));

        List<Client> clients = clientRepository.saveAllAndFlush(mockService.createMockClients(clientAppUsers));
        List<Caretaker> caretakers = caretakerRepository.saveAllAndFlush(mockService.createMockCaretakers(caretakerAppUsers));
        ratingRepository.saveAllAndFlush(mockService.createMockRatings(caretakers, clients));

        log.info("Mock data created successfully!");
    }

    private boolean shouldSkipInit() {
        return caretakerRepository.count() != 0 &&
                appUserRepository.count() != 0 &&
                ratingRepository.count() != 0;

    }
}

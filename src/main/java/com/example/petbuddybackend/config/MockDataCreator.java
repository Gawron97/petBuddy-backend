package com.example.petbuddybackend.config;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class MockDataCreator {

    private static final int CARETAKER_COUNT = 50;
    private static final Faker faker = new Faker();

    private final CaretakerRepository caretakerRepository;
    private final AppUserRepository appUserRepository;

    @PostConstruct
    public void createMockData() {
        if (shouldSkipInit()) {
            return;
        }
        log.info("Creating mock data in database...");

        var appUsers = createAndSaveAppUsers(CARETAKER_COUNT);
        createAndSaveMockCaretakers(appUsers);

        log.info("Mock data created successfully!");
    }

    private List<AppUser> createAndSaveAppUsers(int count) {
        List<AppUser> caretakers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            caretakers.add(createAppUser());
        }

        return appUserRepository.saveAllAndFlush(caretakers);
    }

    private AppUser createAppUser() {
        return AppUser.builder()
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .build();
    }

    private void createAndSaveMockCaretakers(List<AppUser> users) {
        List<Caretaker> caretakers = new ArrayList<>();

        for (AppUser user : users) {
            caretakers.add(createCaretaker(user));
        }

        caretakerRepository.saveAllAndFlush(caretakers);
    }

    private Caretaker createCaretaker(AppUser user) {
        Address address = Address.builder()
                .city(faker.address().city())
                .street(faker.address().streetName())
                .zipCode(faker.address().zipCode())
                .voivodeship(randomVoivodeship())
                .city(faker.address().city())
                .buildingNumber(faker.address().buildingNumber())
                .apartmentNumber(faker.address().secondaryAddress())
                .build();

        return Caretaker.builder()
                .email(user.getEmail())
                .address(address)
                .animalsTakenCareOf(randomAnimalTypes())
                .description(faker.lorem().sentence())
                .phoneNumber(faker.phoneNumber().cellPhone())
                .build();
    }

    private Voivodeship randomVoivodeship() {
        Voivodeship[] voivodeships = Voivodeship.values();
        return voivodeships[faker.random().nextInt(voivodeships.length)];
    }

    private Set<AnimalType> randomAnimalTypes() {
        AnimalType[] animalTypes = AnimalType.values();

        int numberOfTypes = faker.random().nextInt(1, (animalTypes.length + 1) / 2);
        Set<AnimalType> types = new HashSet<>(numberOfTypes);

        while (types.size() < numberOfTypes) {
            types.add(animalTypes[faker.random().nextInt(animalTypes.length)]);
        }

        return types;
    }

    private boolean shouldSkipInit() {
        return caretakerRepository.count() != 0 &&
                appUserRepository.count() != 0;

    }
}

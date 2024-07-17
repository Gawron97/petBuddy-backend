package com.example.petbuddybackend.service.mock;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.github.javafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Profile("dev | test")
@Service
public class MockService {

    private final Faker faker = new Faker();


    public List<AppUser> createAppUsers(int count) {
        List<AppUser> caretakers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            caretakers.add(createAppUser());
        }

        return caretakers;
    }

    public AppUser createAppUser() {
        return AppUser.builder()
                .username(faker.name().username())
                .email(faker.internet().emailAddress())
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .build();
    }

    public List<Caretaker> createMockCaretakers(List<AppUser> users) {
        List<Caretaker> caretakers = new ArrayList<>();

        for (AppUser user : users) {
            caretakers.add(createCaretaker(user));
        }

        return caretakers;
    }

    public Caretaker createCaretaker(AppUser user) {
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

    public Voivodeship randomVoivodeship() {
        Voivodeship[] voivodeships = Voivodeship.values();
        return voivodeships[faker.random().nextInt(voivodeships.length)];
    }

    public Set<AnimalType> randomAnimalTypes() {
        AnimalType[] animalTypes = AnimalType.values();

        int numberOfTypes = faker.random().nextInt(1, (animalTypes.length + 1) / 2);
        Set<AnimalType> types = new HashSet<>(numberOfTypes);

        while (types.size() < numberOfTypes) {
            types.add(animalTypes[faker.random().nextInt(animalTypes.length)]);
        }

        return types;
    }
}

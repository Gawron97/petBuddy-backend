package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.AnimalTakenCareOf;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;

import java.util.Arrays;
import java.util.List;

import static com.example.petbuddybackend.entity.animal.AnimalType.*;

public final class MockUtils {
    
    private MockUtils() {
    }


    public static Address createMockAddress(Voivodeship voivodeship, String city) {
        return Address.builder()
                .city(city)
                .voivodeship(voivodeship)
                .street("street")
                .zipCode("12-123")
                .buildingNumber("5A")
                .apartmentNumber("10")
                .build();
    }

    public static Address createMockAddress() {
        return createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa");
    }

    public static Caretaker createMockCaretaker(String name, String surname, String email, List<AnimalTakenCareOf> animals, Address address) {
        AppUser accountData = AppUser.builder()
                .email(email)
                .name(name)
                .surname(surname)
                .build();

        Caretaker caretaker = Caretaker.builder()
                .accountData(accountData)
                .address(address)
                .description("description")
                .phoneNumber("number")
                .avgRating(4.5f)
                .build();

        animals = animals.stream()
                .peek(animal -> animal.setCaretaker(caretaker))
                .toList();

        caretaker.setAnimalsTakenCareOf(animals);
        return caretaker;
    }

    public static Caretaker createMockCaretaker() {
        return createMockCaretaker(
                "name",
                "surname",
                "email",
                animalsOfTypes(CAT, DOG),
                createMockAddress()
        );
    }

    public static List<Caretaker> createMockCaretakers() {
        return List.of(
                MockUtils.createMockCaretaker("John", "Doe", "testmail@mail.com", animalsOfTypes(DOG, CAT, BIRD),
                        createMockAddress(Voivodeship.SLASKIE, "Katowice")),
                MockUtils.createMockCaretaker("Jane", "Doe", "another@mail.com", animalsOfTypes(DOG),
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa")),
                MockUtils.createMockCaretaker("John", "Smith", "onceagain@mail.com", animalsOfTypes(REPTILE),
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa"))
        );
    }

    public static AnimalTakenCareOf animalOfType(AnimalType animalType) {
        return AnimalTakenCareOf.builder()
                .animalType(animalType)
                .build();
    }

    public static List<AnimalTakenCareOf> animalsOfTypes(AnimalType... animalTypes) {
        return Arrays.stream(animalTypes)
                .map(MockUtils::animalOfType)
                .toList();
    }

    public static Client createMockClient() {
        return Client.builder()
                .accountData(AppUser.builder()
                        .email("clientEmail")
                        .name("clientName")
                        .surname("clientSurname")
                        .build())
                .build();
    }

    public static Rating createMockRating(Caretaker caretaker, Client client) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .caretaker(caretaker)
                .client(client)
                .rating(5)
                .comment("comment")
                .build();
    }
}

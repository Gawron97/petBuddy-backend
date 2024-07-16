package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;

import java.util.List;
import java.util.Set;

import static com.example.petbuddybackend.entity.animal.AnimalType.*;

public final class MockUtils {
    
    private MockUtils() {
    }


    public static Address createMockAddress(Voivodeship voivodeship, String city) {
        return Address.builder()
                .city(city)
                .voivodeship(voivodeship)
                .street("street")
                .postalCode("12-123")
                .buildingNumber("5A")
                .apartmentNumber("10")
                .build();
    }

    public static Address createMockAddress() {
        return createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa");
    }

    public static Caretaker createMockCaretaker(String name, String surname, String email, List<AnimalType> animalTypes, Address address) {
        var accountData = AppUser.builder()
                .email(email)
                .name(name)
                .surname(surname)
                .build();

        return Caretaker.builder()
                .accountData(accountData)
                .address(address)
                .description("description")
                .phoneNumber("number")
                .email("email")
                .animalsTakenCareOf(Set.copyOf(animalTypes))
                .build();
    }

    public static Caretaker createMockCaretaker() {
        return createMockCaretaker(
                "name",
                "surname",
                "email",
                List.of(AnimalType.CAT, AnimalType.DOG),
                createMockAddress()
        );
    }

    public static List<Caretaker> createMockCaretakers() {
        return List.of(
                MockUtils.createMockCaretaker("John", "Doe", "testmail@mail.com", List.of(DOG, CAT, BIRD),
                        createMockAddress(Voivodeship.SLASKIE, "Katowice")),
                MockUtils.createMockCaretaker("Jane", "Doe", "another@mail.com", List.of(DOG),
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa")),
                MockUtils.createMockCaretaker("John", "Smith", "onceagain@mail.com", List.of(REPTILE),
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa"))
        );
    }
}

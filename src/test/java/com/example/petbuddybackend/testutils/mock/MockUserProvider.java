package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockUserProvider {

    public static Address createMockAddress(Voivodeship voivodeship, String city) {
        return Address.builder()
                .city(city)
                .voivodeship(voivodeship)
                .street("street")
                .zipCode("12-123")
                .streetNumber("5A")
                .apartmentNumber("10")
                .build();
    }

    public static Address createMockAddress() {
        return createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa");
    }

    public static Caretaker createMockCaretaker(String name, String surname, String email, Address address) {
        AppUser accountData = createMockAppUser(name, surname, email);

        return Caretaker.builder()
                .email(email)
                .accountData(accountData)
                .address(address)
                .description("description")
                .phoneNumber("number")
                .avgRating(4.5f)
                .build();
    }

    public static Caretaker createMockCaretaker(String email) {
        return createMockCaretaker(
                "name",
                "surname",
                email,
                createMockAddress()
        );
    }

    public static Caretaker createMockCaretaker() {
        return createMockCaretaker("caretakerEmail");
    }

    public static List<Caretaker> createMockCaretakers() {
        return List.of(
                createMockCaretaker("John", "Doe", "testmail@mail.com",
                        createMockAddress(Voivodeship.SLASKIE, "Katowice")),
                createMockCaretaker("Jane", "Doe", "another@mail.com",
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa")),
                createMockCaretaker("John", "Smith", "onceagain@mail.com",
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa"))
        );
    }

    public static Client createMockClient(String newClientEmail) {
        AppUser accountData = createMockAppUser("clientName", "clientSurname", newClientEmail);

        return Client.builder()
                .email(newClientEmail)
                .accountData(accountData)
                .build();
    }

    public static Client createMockClient() {
        return createMockClient("clientEmail");
    }

    public static Client createMockClient(String name, String surname, String email) {
        AppUser accountData = createMockAppUser(name, surname, email);

        return Client.builder()
                .email(email)
                .accountData(accountData)
                .build();
    }

    public static AppUser createMockAppUser(String name, String surname, String email) {
        return AppUser.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .build();
    }

    public static AppUser createMockAppUser() {
        return createMockAppUser("name", "surname", "email");
    }

    public static AppUser createMockAppUser(PhotoLink photoLink) {
        AppUser user = createMockAppUser("appUser_name", "appUser_surname", "appUser_email");
        user.setProfilePicture(photoLink);
        return user;
    }

    public static PhotoLink createMockPhotoLink() {
        return PhotoLink.builder()
                .blob("blobexample")
                .url("http://example.com")
                .build();
    }
}

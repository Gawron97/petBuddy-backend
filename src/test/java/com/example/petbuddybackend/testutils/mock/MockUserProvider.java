package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.utils.provider.geolocation.dto.Coordinates;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                .latitude(BigDecimal.valueOf(52.22977))
                .longitude(BigDecimal.valueOf(21.01178))
                .build();
    }

    public static Address createMockAddress() {
        return createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa");
    }

    public static Coordinates createMockCoordinates() {
        return Coordinates.builder()
                .latitude(BigDecimal.valueOf(52.2297))
                .longitude(BigDecimal.valueOf(21.0118))
                .build();
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

    public static Client createMockClient(String clientEmail) {
        AppUser accountData = createMockAppUser("clientName", "clientSurname", clientEmail);

        return Client.builder()
                .email(clientEmail)
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

    public static Client createMockClientWithPhoto(String clientEmail) {
        AppUser accountData = createMockAppUserWithPhoto("clientName", "clientSurname", clientEmail);

        return Client.builder()
                .email(clientEmail)
                .accountData(accountData)
                .build();
    }

    public static Caretaker createMockCaretakerWithPhoto(String caretakerEmail) {
        AppUser accountData = createMockAppUserWithPhoto("caretakerName", "caretakerSurname", caretakerEmail);

        return Caretaker.builder()
                .email(caretakerEmail)
                .accountData(accountData)
                .address(createMockAddress())
                .description("description")
                .phoneNumber("number")
                .avgRating(4.5f)
                .build();
    }

    public static AppUser createMockAppUser(String name, String surname, String email) {
        return AppUser.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .build();
    }

    public static AppUser createMockAppUserWithPhoto(String name, String surname, String email) {
        return AppUser.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .profilePicture(createMockPhotoLink())
                .build();
    }

    public static AppUser createMockAppUserWithPhoto() {
        AppUser user = createMockAppUser();
        user.setProfilePicture(createMockPhotoLink());
        return user;
    }

    public static AppUser createMockAppUser() {
        return createMockAppUser("name", "surname", "email");
    }

    public static AppUser createMockAppUser(PhotoLink photoLink) {
        AppUser user = createMockAppUser("appUser_name", "appUser_surname", "appUser_email");
        user.setProfilePicture(photoLink);
        return user;
    }

    public static PhotoLink createMockPhotoLink(String blob) {
        return PhotoLink.builder()
                .blob(blob)
                .url("http://example.com")
                .urlExpiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    public static PhotoLink createMockPhotoLink() {
        return createMockPhotoLink("blobexample");
    }
}

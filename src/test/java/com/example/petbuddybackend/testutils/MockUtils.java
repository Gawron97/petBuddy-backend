package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
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

    public static Caretaker createMockCaretaker(String name, String surname, String email, Address address) {
        AppUser accountData = AppUser.builder()
                .email(email)
                .name(name)
                .surname(surname)
                .build();

        return Caretaker.builder()
                .email(email)
                .accountData(accountData)
                .address(address)
                .description("description")
                .phoneNumber("number")
                .avgRating(4.5f)
                .build();
    }

    public static Caretaker createMockCaretaker() {
        return createMockCaretaker(
                "name",
                "surname",
                "email",
                createMockAddress()
        );
    }

    public static List<Caretaker> createMockCaretakers() {
        return List.of(
                MockUtils.createMockCaretaker("John", "Doe", "testmail@mail.com",
                        createMockAddress(Voivodeship.SLASKIE, "Katowice")),
                MockUtils.createMockCaretaker("Jane", "Doe", "another@mail.com",
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa")),
                MockUtils.createMockCaretaker("John", "Smith", "onceagain@mail.com",
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa"))
        );
    }


    public static Client createMockClient() {
        String email = "clientEmail";

        return Client.builder()
                .email(email)
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

    public static List<Offer> createMockOffers(Caretaker caretaker, List<Animal> animals) {

        return animals.stream()
                .map(animal -> createMockOffer(caretaker, animal))
                .toList();

    }

    public static Offer createMockOffer(Caretaker caretaker, Animal animal) {
        return Offer.builder()
                .caretaker(caretaker)
                .animal(animal)
                .build();
    }

    public static List<Offer> createMockOffers(List<Caretaker> caretakers, List<Animal> animals) {

        Animal cat = animals.stream()
                .filter(animal -> animal.getAnimalType().equals("CAT"))
                .findFirst()
                .orElseThrow();

        Animal dog = animals.stream()
                .filter(animal -> animal.getAnimalType().equals("DOG"))
                .findFirst()
                .orElseThrow();

        Animal bird = animals.stream()
                .filter(animal -> animal.getAnimalType().equals("BIRD"))
                .findFirst()
                .orElseThrow();

        Animal reptile = animals.stream()
                .filter(animal -> animal.getAnimalType().equals("REPTILE"))
                .findFirst()
                .orElseThrow();

        return List.of(
                createMockOffer(caretakers.get(0), dog),
                createMockOffer(caretakers.get(0), cat),
                createMockOffer(caretakers.get(0), bird),
                createMockOffer(caretakers.get(1), dog),
                createMockOffer(caretakers.get(1), reptile));

    }

    public static Animal createAnimal(String animalType) {
        return Animal.builder()
                .animalType(animalType)
                .build();
    }

    public static AnimalAttribute createAnimalAttribute(String attributeName, String attributeValue, Animal animal) {
        return AnimalAttribute.builder()
                .animal(animal)
                .attributeName(attributeName)
                .attributeValue(attributeValue)
                .build();
    }

    public static OfferOption createOfferOption(AnimalAttribute animalAttribute) {
        return OfferOption.builder()
                .animalAttribute(animalAttribute)
                .build();
    }

    public static OfferConfiguration createOfferConfiguration(List<OfferOption> offerOptions) {
        return OfferConfiguration.builder()
                .description("description")
                .dailyPrice(10.0)
                .offerOptions(offerOptions)
                .build();
    }

    public static AnimalAmenity createAnimalAmenity(Animal animal, String amenity) {
        return AnimalAmenity.builder()
                .animal(animal)
                .amenity(Amenity.builder().amenity(amenity).build())
                .build();
    }

    public static void createComplexMockOfferForCaretaker(Caretaker caretaker) {

        Animal dog = createAnimal("DOG");

        AnimalAttribute dogAttribute = createAnimalAttribute("SIZE", "BIG", dog);
        AnimalAttribute dogAttribute2 = createAnimalAttribute("SEX", "MALE", dog);

        List<OfferOption> offerOptions = List.of(
                createOfferOption(dogAttribute),
                createOfferOption(dogAttribute2)
        );

        OfferConfiguration offerConfiguration = createOfferConfiguration(offerOptions);
        List<AnimalAmenity> animalAmenities = Arrays.asList(
                createAnimalAmenity(dog, "WALKING"),
                createAnimalAmenity(dog, "FEEDING")
        );

        Offer offer = Offer.builder()
                .animal(dog)
                .animalAmenities(animalAmenities)
                .offerConfigurations(List.of(offerConfiguration))
                .build();
        caretaker.setOffers(List.of(offer));

    }
}

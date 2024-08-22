package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

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
                .description("description")
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

    public static Animal createMockAnimal(String animalType) {
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

    public static OfferConfiguration createOfferConfiguration(Offer offer, Double price) {
        return OfferConfiguration.builder()
                .description("description")
                .dailyPrice(price)
                .offer(offer)
                .build();
    }

    public static OfferConfiguration createOfferConfiguration(Offer offer, List<AnimalAttribute> animalAttributes) {
        OfferConfiguration offerConfiguration = createOfferConfiguration(offer, 10.0);
        List<OfferOption> offerOptions = createOfferOptions(animalAttributes, offerConfiguration);

        offerConfiguration.setOfferOptions(new ArrayList<>(offerOptions));
        return offerConfiguration;
    }


    public static AnimalAmenity createAnimalAmenity(Animal animal, String amenity) {
        return AnimalAmenity.builder()
                .animal(animal)
                .amenity(Amenity.builder().name(amenity).build())
                .build();
    }

    public static void createComplexMockOfferForCaretaker(Caretaker caretaker) {

        Animal dog = createMockAnimal("DOG");

        AnimalAttribute dogAttribute = createAnimalAttribute("SIZE", "BIG", dog);
        AnimalAttribute dogAttribute2 = createAnimalAttribute("SEX", "MALE", dog);

        List<OfferOption> offerOptions = List.of(
                createOfferOption(dogAttribute),
                createOfferOption(dogAttribute2)
        );

        OfferConfiguration offerConfiguration = createOfferConfiguration(offerOptions);
        Set<AnimalAmenity> animalAmenities = new HashSet<>(Arrays.asList(
                createAnimalAmenity(dog, "toys"),
                createAnimalAmenity(dog, "FEEDING")
        ));

        Offer offer = Offer.builder()
                .animal(dog)
                .animalAmenities(animalAmenities)
                .offerConfigurations(Arrays.asList(offerConfiguration))
                .build();
        caretaker.setOffers(Arrays.asList(offer));

    }

    public static List<OfferOption> createOfferOptions(List<AnimalAttribute> animalAttributes,
                                                       OfferConfiguration offerConfiguration) {

        return animalAttributes.stream()
                .map(animalAttribute -> createOfferOption(animalAttribute, offerConfiguration))
                .collect(Collectors.toList());

    }

    public static OfferOption createOfferOption(AnimalAttribute animalAttribute,
                                                OfferConfiguration offerConfiguration) {
        return OfferOption.builder()
                .animalAttribute(animalAttribute)
                .offerConfiguration(offerConfiguration)
                .build();
    }

    public static Offer createComplexMockOfferForCaretaker(Caretaker caretaker,
                                                           Animal animal,
                                                           List<AnimalAttribute> animalAttributes,
                                                           Double price,
                                                           List<AnimalAmenity> animalAmenities) {

        Offer offer = Offer.builder()
                .animal(animal)
                .caretaker(caretaker)
                .description("description")
                .build();

        if(CollectionUtil.isNotEmpty(animalAttributes)) {
            OfferConfiguration offerConfiguration = createOfferConfiguration(offer, price);
            List<OfferOption> offerOptions = createOfferOptions(animalAttributes, offerConfiguration);

            offerConfiguration.setOfferOptions(new ArrayList<>(offerOptions));
            if(CollectionUtil.isNotEmpty(offer.getOfferConfigurations())) {
                offer.getOfferConfigurations().add(offerConfiguration);
            } else {
                offer.setOfferConfigurations(new ArrayList<>(List.of(offerConfiguration)));
            }

        }
        if(CollectionUtil.isNotEmpty(animalAmenities)) {
            if(CollectionUtil.isNotEmpty(offer.getAnimalAmenities())) {
                offer.getAnimalAmenities().addAll(animalAmenities);
            } else {
                offer.setAnimalAmenities(new HashSet<>(animalAmenities));
            }
        }

        caretaker.setOffers(new ArrayList<>(List.of(offer)));

        return offer;

    }

    public static Offer createComplexMockOfferForCaretaker(Caretaker caretaker,
                                                           Animal animal,
                                                           List<AnimalAttribute> animalAttributes,
                                                           Double price,
                                                           List<AnimalAmenity> animalAmenities,
                                                           Offer offer) {


        if(CollectionUtil.isNotEmpty(animalAttributes)) {
            OfferConfiguration offerConfiguration = createOfferConfiguration(offer, price);
            List<OfferOption> offerOptions = createOfferOptions(animalAttributes, offerConfiguration);

            offerConfiguration.setOfferOptions(new ArrayList<>(offerOptions));
            if(CollectionUtil.isNotEmpty(offer.getOfferConfigurations())) {
                offer.getOfferConfigurations().add(offerConfiguration);
            } else {
                offer.setOfferConfigurations(new ArrayList<>(List.of(offerConfiguration)));
            }

        }
        if(CollectionUtil.isNotEmpty(animalAmenities)) {
            if(CollectionUtil.isNotEmpty(offer.getAnimalAmenities())) {
                offer.getAnimalAmenities().addAll(animalAmenities);
            } else {
                offer.setAnimalAmenities(new HashSet<>(animalAmenities));
            }
        }

        caretaker.setOffers(new ArrayList<>(List.of(offer)));

        return offer;

    }

    public static AppUser createMockAppUser() {

        return AppUser.builder()
                .name("Imie")
                .surname("Nazwisko")
                .email("email")
                .build();

    }

    public static JwtAuthenticationToken createJwtToken(String email, String firstname, String lastname, String username) {
        return new JwtAuthenticationToken(mock(Jwt.class), null, null) {
            @Override
            public Map<String, Object> getTokenAttributes() {
                return Map.of(
                        "email", email,
                        "given_name", firstname,
                        "family_name", lastname,
                        "preferred_username", username
                );
            }
        };

    }

}

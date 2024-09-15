package com.example.petbuddybackend.service.datageneration;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.github.javafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Profile("dev | test")
@Service
public class MockService {

    private final Faker faker = new Faker();


    public List<AppUser> createMockAppUsers(int count) {
        List<AppUser> caretakers = new ArrayList<>(count);

        for(int i = 0; i < count; i++) {
            caretakers.add(createAppUser());
        }

        return caretakers;
    }

    public AppUser createAppUser() {
        return AppUser.builder()
                .email(faker.internet().emailAddress())
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .build();
    }

    public List<Caretaker> createMockCaretakers(List<AppUser> users) {
        List<Caretaker> caretakers = new ArrayList<>();

        for(AppUser user : users) {
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
                .accountData(user)
                .address(address)
                .description(faker.lorem().sentence())
                .phoneNumber(faker.phoneNumber().cellPhone())
                .build();
    }

    public Voivodeship randomVoivodeship() {
        Voivodeship[] voivodeships = Voivodeship.values();
        return voivodeships[faker.random().nextInt(voivodeships.length)];
    }

    public Rating createMockRating(Caretaker caretaker, Client client) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .caretaker(caretaker)
                .client(client)
                .rating(faker.random().nextInt(1, 5))
                .comment(faker.lorem().sentence())
                .build();
    }

    public List<Rating> createMockRatings(List<Caretaker> caretakers, List<Client> clients) {
        List<Rating> ratings = new ArrayList<>(caretakers.size() * clients.size());

        for(Caretaker caretaker : caretakers) {
            for(Client client : clients) {
                ratings.add(createMockRating(caretaker, client));
            }
        }

        return ratings;
    }


    public Client createClient(AppUser user) {
        return Client.builder()
                .email(user.getEmail())
                .build();
    }

    public List<Client> createMockClients(List<AppUser> users) {
        List<Client> clients = new ArrayList<>();

        for(AppUser user : users) {
            clients.add(createClient(user));
        }

        return clients;
    }

    public List<Offer> createMockOffers(List<Caretaker> caretakers, List<Animal> animals, int caretakerOfferCount) {
        List<Offer> offers = new ArrayList<>();

        for(Caretaker caretaker : caretakers) {
            offers.addAll(createCaretakerOffers(caretaker, animals, caretakerOfferCount));
        }

        return offers;

    }

    private List<Offer> createCaretakerOffers(Caretaker caretaker, List<Animal> animals, int caretakerOfferCount) {

        List<Offer> offers = new ArrayList<>();

        List<Animal> uniqueRandomAnimals = getSomeRandomUniqueAnimals(animals, caretakerOfferCount);

        uniqueRandomAnimals.forEach(animal -> offers.add(createCaretakerOffer(caretaker, animal)));
        caretaker.getOffers().addAll(offers);
        return offers;

    }

    private Offer createCaretakerOffer(Caretaker caretaker, Animal animal) {

        return Offer.builder()
                .caretaker(caretaker)
                .animal(animal)
                .description(faker.lorem().sentence())
                .build();

    }

    private List<Animal> getSomeRandomUniqueAnimals(List<Animal> animals, int count) {
        if(count > animals.size()) {
            return animals;
        }

        Set<Animal> uniqueAnimals = new HashSet<>();
        while(uniqueAnimals.size() < count) {
            uniqueAnimals.add(animals.get(faker.random().nextInt(animals.size())));
        }
        return new ArrayList<>(uniqueAnimals);

    }

    public List<OfferConfiguration> createMockOffersConfigurations(List<Offer> offers, int caretakerOfferConfigurationCount) {
        List<OfferConfiguration> offerConfigurations = new ArrayList<>();

        for(Offer offer : offers) {
            offerConfigurations.addAll(createMockOfferConfigurations(offer, caretakerOfferConfigurationCount));
        }

        return offerConfigurations;

    }

    private List<OfferConfiguration> createMockOfferConfigurations(Offer offer, int caretakerOfferConfigurationCount) {

        List<OfferConfiguration> offerConfigurations = new ArrayList<>();

        for(int i = 0; i < caretakerOfferConfigurationCount; i++) {
            offerConfigurations.add(createMockOfferConfiguration(offer));
        }
        offer.setOfferConfigurations(offerConfigurations);

        return offerConfigurations;

    }

    private OfferConfiguration createMockOfferConfiguration(Offer offer) {
        return OfferConfiguration.builder()
                .offer(offer)
                .dailyPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 100)))
                .description(faker.lorem().sentence())
                .build();
    }

    public List<OfferOption> createMockOfferConfigurationsOptionsForCaretakers(List<Caretaker> caretakers,
                                                                               List<AnimalAttribute> animalAttributes,
                                                                               int optionsInConfigurationCount) {
        List<OfferOption> offerOptions = new ArrayList<>();

        for(Caretaker caretaker : caretakers) {
            offerOptions.addAll(createMockOffersConfigurationsOptions(caretaker.getOffers(), animalAttributes, optionsInConfigurationCount));
        }
        return offerOptions;

    }

    private List<OfferOption> createMockOffersConfigurationsOptions(List<Offer> offers,
                                                                    List<AnimalAttribute> animalAttributes,
                                                                    int optionsInConfigurationCount) {
        List<OfferOption> offerOptions = new ArrayList<>();

        for(Offer offer : offers) {
            offerOptions.addAll(createMockOfferConfigurationsOptions(offer, animalAttributes, optionsInConfigurationCount));
        }

        return offerOptions;

    }

    private List<OfferOption> createMockOfferConfigurationsOptions(Offer offer,
                                                                   List<AnimalAttribute> animalAttributes,
                                                                   int optionsInConfigurationCount) {
        List<OfferOption> offerOptions = new ArrayList<>();

        List<List<AnimalAttribute>> uniqueRandomListOfAnimalAttributes =
                getSomeRandomUniqueListOfAnimalAttributes(animalAttributes, offer, optionsInConfigurationCount);

        for(int i = 0; i < offer.getOfferConfigurations().size(); i++) {
            OfferConfiguration offerConfiguration = offer.getOfferConfigurations().get(i);
            List<AnimalAttribute> uniqueRandomAnimalAttributes = uniqueRandomListOfAnimalAttributes.get(i);

            offerOptions.addAll(createMockOfferConfigurationOptions(offerConfiguration, uniqueRandomAnimalAttributes));

        }
        return offerOptions;

    }

    private List<OfferOption> createMockOfferConfigurationOptions(OfferConfiguration offerConfiguration,
                                                                  List<AnimalAttribute> uniqueRandomAnimalAttributes) {
        List<OfferOption> offerOptions = new ArrayList<>();

        for(AnimalAttribute animalAttribute : uniqueRandomAnimalAttributes) {
            offerOptions.add(createMockOfferConfigurationOption(offerConfiguration, animalAttribute));
        }
        offerConfiguration.setOfferOptions(offerOptions);

        return offerOptions;

    }

    private OfferOption createMockOfferConfigurationOption(OfferConfiguration offerConfiguration, AnimalAttribute animalAttribute) {
        return OfferOption.builder()
                .offerConfiguration(offerConfiguration)
                .animalAttribute(animalAttribute)
                .build();
    }

    private List<List<AnimalAttribute>> getSomeRandomUniqueListOfAnimalAttributes(List<AnimalAttribute> animalAttributes,
                                                                                  Offer offer,
                                                                                  int optionsInConfigurationCount) {


        List<AnimalAttribute> animalAttributesForAnimalType =
                getAnimalAttributeForAnimalType(offer.getAnimal().getAnimalType(), animalAttributes);

        Set<List<AnimalAttribute>> uniqueListOfAnimalAttributes = new HashSet<>();
        while(uniqueListOfAnimalAttributes.size() < offer.getOfferConfigurations().size()) {
            uniqueListOfAnimalAttributes.add(
                    getSomeRandomUniqueAnimalAttributes(animalAttributesForAnimalType, optionsInConfigurationCount));
        }
        return new ArrayList<>(uniqueListOfAnimalAttributes);

    }

    private List<AnimalAttribute> getSomeRandomUniqueAnimalAttributes(List<AnimalAttribute> animalAttributes,
                                                                      int count) {

        if(count > animalAttributes.size()) {
            return List.of(animalAttributes.get(faker.random().nextInt(animalAttributes.size())));
        }

        Set<AnimalAttribute> uniqueAnimalAttributes = new HashSet<>();
        while(uniqueAnimalAttributes.size() < count) {
            uniqueAnimalAttributes.add(animalAttributes.get(faker.random().nextInt(animalAttributes.size())));
        }
        return uniqueAnimalAttributes.stream().sorted(Comparator.comparingLong(AnimalAttribute::getId)).toList();

    }

    private List<AnimalAttribute> getAnimalAttributeForAnimalType(String animalType, List<AnimalAttribute> animalAttributes) {
        return animalAttributes.stream()
                .filter(animalAttribute -> animalAttribute.getAnimal().getAnimalType().equals(animalType))
                .toList();
    }

    public List<Offer> createMockOffersAmenities(List<Offer> offers, List<AnimalAmenity> animalAmenities,
                                                 int ANIMAL_AMENITY_IN_OFFER_COUNT) {

        return offers.stream()
                .map(offer ->
                        createMockOfferAmenities(offer,
                                getAnimalAmenitiesForAnimalType(offer.getAnimal().getAnimalType(), animalAmenities),
                                ANIMAL_AMENITY_IN_OFFER_COUNT))
                .toList();

    }

    private Offer createMockOfferAmenities(Offer offer, List<AnimalAmenity> animalAmenities, int animalAmenityCount) {

        Set<AnimalAmenity> uniqueRandomAnimalAmenities = getSomeRandomUniqueAnimalAmenities(animalAmenities, animalAmenityCount);
        offer.setAnimalAmenities(uniqueRandomAnimalAmenities);
        return offer;

    }

    private List<AnimalAmenity> getAnimalAmenitiesForAnimalType(String animalType, List<AnimalAmenity> animalAmenities) {
        return animalAmenities.stream()
                .filter(animalAmenity -> animalAmenity.getAnimal().getAnimalType().equals(animalType))
                .toList();
    }

    private Set<AnimalAmenity> getSomeRandomUniqueAnimalAmenities(List<AnimalAmenity> animalAmenities, int count) {
        Set<AnimalAmenity> amenities = new HashSet<>();

        if(animalAmenities.size() < count) {
            return new HashSet<>(animalAmenities);
        }

        while(amenities.size() < count) {
            amenities.add(animalAmenities.get(faker.random().nextInt(animalAmenities.size())));
        }

        return new HashSet<>(amenities);
    }

    public List<ChatMessage> generateMessages(int count, Client client, Caretaker caretaker, ChatRoom chatRoom) {
        count = count < 1 ? 2 : count;
        List<ChatMessage> messages = new ArrayList<>(count);

        for(int i = 0; i < count / 2; i++) {
            messages.add(ChatMessage.builder()
                    .sender(client.getAccountData())
                    .content(faker.lorem().sentence())
                    .chatRoom(chatRoom)
                    .build());
        }

        for(int i = 0; i < count / 2; i++) {
            messages.add(ChatMessage.builder()
                    .sender(caretaker.getAccountData())
                    .content(faker.lorem().sentence())
                    .chatRoom(chatRoom)
                    .build());
        }

        return messages;
    }

    public List<Care> createMockCares(List<Client> clients, List<Caretaker> caretakers, List<Animal> animals,
                                      List<AnimalAttribute> animalAttributes, int count) {

        List<Care> cares = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            Client client = clients.get(faker.random().nextInt(clients.size()));
            Caretaker caretaker = caretakers.get(faker.random().nextInt(caretakers.size()));
            Animal animal = animals.get(faker.random().nextInt(animals.size()));
            List<AnimalAttribute> availableAnimalAttributes = getAnimalAttributeForAnimalType(animal.getAnimalType(), animalAttributes);
            Set<AnimalAttribute> animalAttributesForCare =
                    new HashSet<>(getSomeRandomUniqueAnimalAttributes(availableAnimalAttributes, faker.random().nextInt(availableAnimalAttributes.size())));
            cares.add(createMockCare(client, caretaker, animal, animalAttributesForCare));
        }
        return cares;
    }

    private Care createMockCare(Client client, Caretaker caretaker, Animal animal, Set<AnimalAttribute> animalAttributesForCare) {

        LocalDate careStart = getRandomDateBetween(
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        ).toLocalDate();

        LocalDate careEnd = getRandomDateBetween(
                careStart,
                LocalDate.ofEpochDay(careStart.toEpochDay() + 10)
        ).toLocalDate();

        return Care.builder()
                .careStart(careStart)
                .careEnd(careEnd)
                .caretakerStatus(CareStatus.values()[faker.random().nextInt(CareStatus.values().length)])
                .clientStatus(CareStatus.values()[faker.random().nextInt(CareStatus.values().length)])
                .description(faker.lorem().sentence())
                .dailyPrice(BigDecimal.valueOf(faker.number().randomDouble(2, 10, 100)))
                .animal(animal)
                .animalAttributes(animalAttributesForCare)
                .caretaker(caretaker)
                .client(client)
                .build();

    }

    private LocalDateTime getRandomDateBetween(LocalDate minDate, LocalDate maxDate) {
        long minDay = minDate.toEpochDay();
        long maxDay = maxDate.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay + 1, maxDay);
        return LocalDate.ofEpochDay(randomDay).atTime(faker.random().nextInt(0, 23), faker.random().nextInt(0, 59));
    }

    public Set<Availability> createMockAvailabilitiesForOffers(List<Offer> offers) {

        Set<Availability> availabilities = new HashSet<>();
        for(Offer offer : offers) {
            Set<Availability> availabilitiesForOffer = createMockAvailabilitiesForOffer(offer);
            offer.setAvailabilities(availabilitiesForOffer);
            availabilities.addAll(availabilitiesForOffer);
        }
        return availabilities;
    }

    private Set<Availability> createMockAvailabilitiesForOffer(Offer offer) {

        int availabilitiesCount = faker.random().nextInt(0, 10);
        Set<Availability> availabilities = new HashSet<>();
        while(availabilities.size() < availabilitiesCount) {
            Availability availability = createMockAvailability(offer);
            if(!isOverLapping(availability, availabilities)) {
                availabilities.add(availability);
            }
        }

        return availabilities;

    }

    private Availability createMockAvailability(Offer offer) {

        LocalDateTime availableFrom = getRandomDateBetween(LocalDate.now(),
                LocalDate.now().plusDays(180));

        LocalDateTime availableTo = getRandomDateBetween(availableFrom.toLocalDate(),
                availableFrom.toLocalDate().plusDays(15));

        return Availability.builder()
                .availableFrom(availableFrom.atZone(ZoneId.systemDefault()))
                .availableTo(availableTo.atZone(ZoneId.systemDefault()))
                .offer(offer)
                .build();
    }

    private boolean isOverLapping(Availability availability, Set<Availability> availabilities) {

        for(Availability existingAvailability : availabilities) {
            if(availability.getAvailableFrom().isBefore(existingAvailability.getAvailableTo()) &&
                    availability.getAvailableTo().isAfter(existingAvailability.getAvailableFrom())) {
                return true;
            }
        }

        return false;
    }

}

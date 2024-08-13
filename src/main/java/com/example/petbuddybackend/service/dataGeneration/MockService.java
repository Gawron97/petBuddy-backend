package com.example.petbuddybackend.service.dataGeneration;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
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
import com.github.javafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Profile("dev | test")
@Service
public class MockService {

    private final Faker faker = new Faker();


    public List<AppUser> createMockAppUsers(int count) {
        List<AppUser> caretakers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
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

        for (AppUser user : users) {
            clients.add(createClient(user));
        }

        return clients;
    }

    public List<Offer> createMockOffers(List<Caretaker> caretakers, List<Animal> animals, int caretakerOfferCount) {
        List<Offer> offers = new ArrayList<>();

        for(Caretaker caretaker: caretakers) {
            offers.addAll(createCaretakerOffers(caretaker, animals, caretakerOfferCount));
        }

        return offers;

    }

    private List<Offer> createCaretakerOffers(Caretaker caretaker, List<Animal> animals, int caretakerOfferCount) {

        List<Offer> offers = new ArrayList<>();

        List<Animal> uniqueRandomAnimals = getSomeRandomUniqueAnimals(animals, caretakerOfferCount);

        uniqueRandomAnimals.forEach(animal -> offers.add(createCaretakerOffer(caretaker, animal)));
        caretaker.setOffers(offers);
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

        for(Offer offer: offers) {
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
                .dailyPrice(faker.number().randomDouble(2, 10, 100))
                .description(faker.lorem().sentence())
                .build();
    }

    public List<OfferOption> createMockOfferConfigurationsOptionsForCaretakers(List<Caretaker> caretakers,
                                                                               List<AnimalAttribute> animalAttributes,
                                                                               int optionsInConfigurationCount) {
        List<OfferOption> offerOptions = new ArrayList<>();

        for (Caretaker caretaker: caretakers) {
            offerOptions.addAll(createMockOffersConfigurationsOptions(caretaker.getOffers(), animalAttributes, optionsInConfigurationCount));
        }
        return offerOptions;

    }

    private List<OfferOption> createMockOffersConfigurationsOptions(List<Offer> offers,
                                                                    List<AnimalAttribute> animalAttributes,
                                                                    int optionsInConfigurationCount) {
        List<OfferOption> offerOptions = new ArrayList<>();

        for (Offer offer: offers) {
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

        for(AnimalAttribute animalAttribute: uniqueRandomAnimalAttributes) {
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

        List<AnimalAmenity> uniqueRandomAnimalAmenities = getSomeRandomUniqueAnimalAmenities(animalAmenities, animalAmenityCount);
        offer.setAnimalAmenities(uniqueRandomAnimalAmenities);
        return offer;

    }

    private List<AnimalAmenity> getAnimalAmenitiesForAnimalType(String animalType, List<AnimalAmenity> animalAmenities) {
        return animalAmenities.stream()
                .filter(animalAmenity -> animalAmenity.getAnimal().getAnimalType().equals(animalType))
                .toList();
    }

    private List<AnimalAmenity> getSomeRandomUniqueAnimalAmenities(List<AnimalAmenity> animalAmenities, int count) {
        Set<AnimalAmenity> amenities = new HashSet<>();

        if(animalAmenities.size() < count) {
            return new ArrayList<>(animalAmenities);
        }

        while(amenities.size() < count) {
            amenities.add(animalAmenities.get(faker.random().nextInt(animalAmenities.size())));
        }

        return new ArrayList<>(amenities);

    }

}

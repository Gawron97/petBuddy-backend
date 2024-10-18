package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import lombok.NoArgsConstructor;
import org.keycloak.common.util.CollectionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockOfferProvider {

    public static Offer createMockOffer(Caretaker caretaker, Animal animal) {
        Offer offer = Offer.builder()
                .caretaker(caretaker)
                .animal(animal)
                .description("description")
                .build();
        caretaker.getOffers().add(offer);
        return offer;
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

    public static List<Offer> createMockOffers(Caretaker caretaker, List<Animal> animals) {
        return animals.stream()
                .map(animal -> createMockOffer(caretaker, animal))
                .toList();

    }

    public static OfferOption createOfferOption(AnimalAttribute animalAttribute) {
        return OfferOption.builder()
                .animalAttribute(animalAttribute)
                .build();
    }

    public static OfferConfiguration createOfferConfiguration(List<OfferOption> offerOptions) {
        return OfferConfiguration.builder()
                .description("description")
                .dailyPrice(BigDecimal.valueOf(10.0))
                .offerOptions(offerOptions)
                .build();
    }

    public static OfferConfiguration createOfferConfiguration(Offer offer, BigDecimal price) {
        return OfferConfiguration.builder()
                .description("description")
                .dailyPrice(price)
                .offer(offer)
                .build();
    }

    public static OfferConfiguration createOfferConfiguration(Offer offer, List<AnimalAttribute> animalAttributes) {
        OfferConfiguration offerConfiguration = createOfferConfiguration(offer, BigDecimal.valueOf(10.0));
        List<OfferOption> offerOptions = createOfferOptions(animalAttributes, offerConfiguration);

        offerConfiguration.setOfferOptions(new ArrayList<>(offerOptions));
        return offerConfiguration;
    }

    public static void editComplexMockOfferForCaretaker(Caretaker caretaker) {
        Animal dog = MockAnimalProvider.createMockAnimal("DOG");

        AnimalAttribute dogAttribute = MockAnimalProvider.createAnimalAttribute("SIZE", "BIG", dog);
        AnimalAttribute dogAttribute2 = MockAnimalProvider.createAnimalAttribute("SEX", "MALE", dog);

        List<OfferOption> offerOptions = List.of(
                createOfferOption(dogAttribute),
                createOfferOption(dogAttribute2)
        );

        OfferConfiguration offerConfiguration = createOfferConfiguration(offerOptions);
        Set<AnimalAmenity> animalAmenities = new HashSet<>(Arrays.asList(
                MockAnimalProvider.createAnimalAmenity(dog, "toys"),
                MockAnimalProvider.createAnimalAmenity(dog, "FEEDING")
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

    public static Offer addComplexMockOfferForCaretaker(Caretaker caretaker,
                                                        Animal animal,
                                                        List<AnimalAttribute> animalAttributes,
                                                        BigDecimal price,
                                                        List<AnimalAmenity> animalAmenities,
                                                        Set<Availability> availabilities) {

        Offer offer = Offer.builder()
                .animal(animal)
                .caretaker(caretaker)
                .description("description")
                .build();

        if(CollectionUtil.isNotEmpty(animalAttributes)) {
            OfferConfiguration offerConfiguration = createOfferConfiguration(offer, price);
            List<OfferOption> offerOptions = createOfferOptions(animalAttributes, offerConfiguration);

            offerConfiguration.setOfferOptions(new ArrayList<>(offerOptions));
            offer.getOfferConfigurations().add(offerConfiguration);


        }
        if(CollectionUtil.isNotEmpty(animalAmenities)) {
            offer.getAnimalAmenities().addAll(animalAmenities);
        }

        if(CollectionUtil.isNotEmpty(availabilities)) {
            availabilities.forEach(availability -> availability.setOffer(offer));
            offer.getAvailabilities().addAll(availabilities);
        }

        caretaker.getOffers().add(offer);

        return offer;

    }

    public static Offer addConfigurationAndAmenitiesForMockOffer(Offer offer,
                                                             List<AnimalAttribute> animalAttributes,
                                                             BigDecimal price,
                                                             List<AnimalAmenity> animalAmenities) {


        if(CollectionUtil.isNotEmpty(animalAttributes)) {
            OfferConfiguration offerConfiguration = createOfferConfiguration(offer, price);
            List<OfferOption> offerOptions = createOfferOptions(animalAttributes, offerConfiguration);

            offerConfiguration.setOfferOptions(new ArrayList<>(offerOptions));
            offer.getOfferConfigurations().add(offerConfiguration);


        }
        if(CollectionUtil.isNotEmpty(animalAmenities)) {
            offer.getAnimalAmenities().addAll(animalAmenities);

        }

        return offer;
    }

    public static Offer setMockAvailabilitiesToOffer(Offer offer) {

        return setAvailabilitiesToOffer(offer, Set.of(
                createMockAvailability(offer, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
                createMockAvailability(offer, LocalDate.now().plusDays(5), LocalDate.now().plusDays(10))
        ));
    }

    public static Availability createMockAvailability(Offer offer, LocalDate availableFrom, LocalDate availableTo) {
        return Availability.builder()
                .offer(offer)
                .availableFrom(availableFrom)
                .availableTo(availableTo)
                .build();
    }

    public static Offer setAvailabilitiesToOffer(Offer existingOffer, Set<Availability> availabilities) {

        availabilities.forEach(availability -> availability.setOffer(existingOffer));
        existingOffer.getAvailabilities().clear();
        existingOffer.getAvailabilities().addAll(availabilities);

        return existingOffer;

    }

}

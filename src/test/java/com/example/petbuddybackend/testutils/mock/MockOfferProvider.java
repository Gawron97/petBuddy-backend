package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import lombok.NoArgsConstructor;
import org.keycloak.common.util.CollectionUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockOfferProvider {

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

    public static void createComplexMockOfferForCaretaker(Caretaker caretaker) {
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

    public static Offer createComplexMockOfferForCaretaker(Caretaker caretaker,
                                                           Animal animal,
                                                           List<AnimalAttribute> animalAttributes,
                                                           BigDecimal price,
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
                                                           BigDecimal price,
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
}

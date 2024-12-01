package com.example.petbuddybackend.utils.specification;

import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SpecificationCommons {

    public static final String CREATED_AT = "createdAt";
    public static final String CHAT_ROOM = "chatRoom";
    public static final String CLIENT = "client";
    public static final String CARETAKER = "caretaker";
    public static final String EMAIL = "email";
    public static final String ANIMAL = "animal";
    public static final String ANIMAL_TYPE = "animalType";
    public static final String CARETAKER_STATUS = "caretakerStatus";
    public static final String CLIENT_STATUS = "clientStatus";
    public static final String SUBMITTED_AT = "submittedAt";
    public static final String CARE_START = "careStart";
    public static final String CARE_END = "careEnd";
    public static final String DAILY_PRICE = "dailyPrice";
    public static final String VOIVODESHIP = "voivodeship";
    public static final String ADDRESS = "address";
    public static final String CITY = "city";
    public static final String ACCOUNT_DATA = "accountData";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String ANIMAL_AMENITIES = "animalAmenities";
    public static final String AMENITY = "amenity";
    public static final String ID = "id";
    public static final String OFFER = "offer";
    public static final String PRICE = "dailyPrice";
    public static final String ANIMAL_ATTRIBUTE = "animalAttribute";
    public static final String ATTRIBUTE_NAME = "attributeName";
    public static final String ATTRIBUTE_VALUE = "attributeValue";
    public static final String OFFER_CONFIGURATION = "offerConfiguration";
    public static final String AVAILABILITIES = "availabilities";
    public static final String AVAILABLE_FROM = "availableFrom";
    public static final String AVAILABLE_TO = "availableTo";

    public static <T> Specification<T> personalDataLike(String personalDataLike,
                                                        Function<Root<T>, From<?, ?>> joinProvider) {

        return (root, query, criteriaBuilder) -> {
            From<?, ?> userJoin = joinProvider.apply(root);
            String[] keywords = personalDataLike.toLowerCase().split("\\s+");
            List<Predicate> predicates = new ArrayList<>(keywords.length);

            for(String keyword : keywords) {
                String likePattern = "%" + keyword + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get(ACCOUNT_DATA).get(NAME)), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get(ACCOUNT_DATA).get(SURNAME)), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get(ACCOUNT_DATA).get(EMAIL)), likePattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

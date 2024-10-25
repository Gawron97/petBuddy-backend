package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.availability.AvailabilityFilterDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferConfigurationFilterDTO;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaretakerSpecificationUtils {

    public static final String VOIVODESHIP = "voivodeship";
    public static final String ADDRESS = "address";
    public static final String CITY = "city";
    public static final String ACCOUNT_DATA = "accountData";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String EMAIL = "email";
    public static final String CARETAKER = "caretaker";
    public static final String OFFER_CONFIGURATIONS = "offerConfigurations";
    public static final String ANIMAL = "animal";
    public static final String ANIMAL_TYPE = "animalType";
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


    public static Specification<Caretaker> toSpecification(CaretakerSearchCriteria filters, Set<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction()
        );

        if (StringUtils.hasText(filters.personalDataLike())) {
            spec = spec.and(personalDataLike(filters.personalDataLike()));
        }

        if (StringUtils.hasText(filters.cityLike())) {
            spec = spec.and(cityLike(filters.cityLike()));
        }

        if (filters.voivodeship() != null) {
            spec = spec.and(voivodeshipEquals(filters.voivodeship()));
        }

        if(!offerFilters.isEmpty()) {
            spec = spec.and(offersMatch(offerFilters));
        }

        return spec;
    }

    private static Specification<Caretaker> personalDataLike(String personalDataLike) {
        return (root, query, criteriaBuilder) -> {
            String[] keywords = personalDataLike.toLowerCase().split("\\s+");
            List<Predicate> predicates = new ArrayList<>(keywords.length);

            for (String keyword : keywords) {
                String likePattern = "%" + keyword + "%";
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(ACCOUNT_DATA).get(NAME)), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(ACCOUNT_DATA).get(SURNAME)), likePattern),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(ACCOUNT_DATA).get(EMAIL)), likePattern)
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Caretaker> cityLike(String cityLike) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + cityLike.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get(ADDRESS).get(CITY)), likePattern);
        };
    }

    private static Specification<Caretaker> voivodeshipEquals(Voivodeship voivodeship) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ADDRESS).get(VOIVODESHIP), voivodeship);
    }

    private static Specification<Caretaker> offersMatch(Set<OfferFilterDTO> offerFilters) {
        return (root, query, cb) -> {

            List<Predicate> offerAnimalAndConfigurationsAndAmenitiesAndAvailabilityPredicates = new ArrayList<>();

            for (OfferFilterDTO offerFilter : offerFilters) {
                offerAnimalAndConfigurationsAndAmenitiesAndAvailabilityPredicates.add(offerMatch(root, query, cb, offerFilter));
            }

            // The caretaker must have offers for all animals specified in the filters
            return cb.and(offerAnimalAndConfigurationsAndAmenitiesAndAvailabilityPredicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate offerMatch(Root<Caretaker> root,
                                        CriteriaQuery<?> query,
                                        CriteriaBuilder cb,
                                        OfferFilterDTO offerFilter) {
        Subquery<Long> offerSubquery = query.subquery(Long.class);
        Root<Offer> offerRoot = offerSubquery.from(Offer.class);
        offerSubquery.select(offerRoot.get(ID));

        Predicate caretakerMatch = cb.equal(offerRoot.get(CARETAKER), root);
        Predicate animalTypeMatch = cb.equal(offerRoot.get(ANIMAL).get(ANIMAL_TYPE), offerFilter.animalType());

        List<Predicate> offerPredicates = new ArrayList<>();
        offerPredicates.add(caretakerMatch);
        offerPredicates.add(animalTypeMatch);

        if (!offerFilter.offerConfigurations().isEmpty()) {
            Predicate configurationsMatch = configurationsMatch(offerSubquery, cb, offerRoot, offerFilter.offerConfigurations());
            offerPredicates.add(configurationsMatch);
        }

        if(!offerFilter.amenities().isEmpty()) {
            Predicate amenitiesMatch = amenitiesMatch(offerSubquery, cb, offerRoot, offerFilter.amenities());
            offerPredicates.add(amenitiesMatch);
        }

        if(!offerFilter.availabilities().isEmpty()) {
            Predicate availabilityMatch = availabilityMatchForOffer(root, query, cb, offerFilter);
            offerPredicates.add(availabilityMatch);
        }

        offerSubquery.where(offerPredicates.toArray(new Predicate[0]));

        // The caretaker must have an offer for this animal that meets all the criteria
        return cb.exists(offerSubquery);
    }

    private static Predicate configurationsMatch(Subquery<Long> offerSubquery, CriteriaBuilder cb, Root<Offer> offerRoot,
                                                 Set<OfferConfigurationFilterDTO> configFilters) {
        List<Predicate> configExistsPredicates = new ArrayList<>();

        for (OfferConfigurationFilterDTO configFilter : configFilters) {
            Predicate configExists = configurationExists(offerSubquery, cb, offerRoot, configFilter);
            configExistsPredicates.add(configExists);
        }

        // All configurations specified in the filter must have matching configurations in the offer
        return cb.and(configExistsPredicates.toArray(new Predicate[0]));
    }

    private static Predicate configurationExists(Subquery<Long> offerSubquery, CriteriaBuilder cb, Root<Offer> offerRoot,
                                                 OfferConfigurationFilterDTO configFilter) {
        Subquery<Long> configSubquery = offerSubquery.subquery(Long.class);
        Root<OfferConfiguration> configRoot = configSubquery.from(OfferConfiguration.class);
        configSubquery.select(configRoot.get(ID));

        Predicate offerMatch = cb.equal(configRoot.get(OFFER), offerRoot);
        Predicate pricePredicate = cb.between(configRoot.get(PRICE), configFilter.minPrice(), configFilter.maxPrice());

        List<Predicate> configPredicates = new ArrayList<>();
        configPredicates.add(offerMatch);
        configPredicates.add(pricePredicate);

        if (configFilter.attributes() != null && !configFilter.attributes().isEmpty()) {
            Predicate attributesMatch = attributesMatch(configSubquery, cb, configRoot, configFilter.attributes());
            configPredicates.add(attributesMatch);
        }

        configSubquery.where(configPredicates.toArray(new Predicate[0]));

        // For each configuration filter, there must exist a configuration matching all criteria
        return cb.exists(configSubquery);
    }

    private static Predicate attributesMatch(Subquery<Long> configSubquery, CriteriaBuilder cb,
                                             Root<OfferConfiguration> configRoot, Map<String, Set<String>> attributes) {
        List<Predicate> attributeGroupPredicates = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            Set<String> attributeValues = entry.getValue();

            Predicate attributeGroupPredicate = attributeGroupMatches(configSubquery, cb, configRoot, attributeName, attributeValues);
            attributeGroupPredicates.add(attributeGroupPredicate);
        }

        return cb.and(attributeGroupPredicates.toArray(new Predicate[0]));
    }

    private static Predicate attributeGroupMatches(Subquery<Long> configSubquery, CriteriaBuilder cb,
                                                   Root<OfferConfiguration> configRoot, String attributeName,
                                                   Set<String> attributeValues) {
        // First, check if the offer configuration has any OfferOption with this attributeName
        Subquery<Long> attributeNameSubquery = configSubquery.subquery(Long.class);
        Root<OfferOption> attributeNameOptionRoot = attributeNameSubquery.from(OfferOption.class);
        attributeNameSubquery.select(attributeNameOptionRoot.get(ID));

        Predicate configOptionMatch = cb.equal(attributeNameOptionRoot.get(OFFER_CONFIGURATION), configRoot);
        Predicate attributeNameMatch = cb.equal(attributeNameOptionRoot.get(ANIMAL_ATTRIBUTE).get(ATTRIBUTE_NAME), attributeName);

        attributeNameSubquery.where(cb.and(configOptionMatch, attributeNameMatch));

        // Check if OfferOptions with this attributeName exist
        Predicate hasAttributeNameOptions = cb.exists(attributeNameSubquery);

        // Create predicates to ensure all required attributeValues are present
        List<Predicate> attributeValuePredicates = new ArrayList<>();

        for (String attributeValue : attributeValues) {
            Predicate attributeValueExists = attributeValueExists(configSubquery, cb, configRoot, attributeName, attributeValue);
            attributeValuePredicates.add(attributeValueExists);
        }

        // Combine the attribute value predicates
        Predicate allAttributeValuesMatch = cb.and(attributeValuePredicates.toArray(new Predicate[0]));

        // Final predicate for this attributeName
        // If no OfferOption with this attributeName exists, consider it matching (i.e., can handle all values)
        return cb.or(
                cb.not(hasAttributeNameOptions),
                allAttributeValuesMatch
        );
    }

    private static Predicate attributeValueExists(Subquery<Long> configSubquery, CriteriaBuilder cb, Root<OfferConfiguration> configRoot, String attributeName, String attributeValue) {
        Subquery<Long> optionSubquery = configSubquery.subquery(Long.class);
        Root<OfferOption> optionRoot = optionSubquery.from(OfferOption.class);
        optionSubquery.select(optionRoot.get(ID));

        Predicate optionConfigMatch = cb.equal(optionRoot.get(OFFER_CONFIGURATION), configRoot);
        Predicate optionAttributeNameMatch = cb.equal(optionRoot.get(ANIMAL_ATTRIBUTE).get(ATTRIBUTE_NAME), attributeName);
        Predicate optionAttributeValueMatch = cb.equal(optionRoot.get(ANIMAL_ATTRIBUTE).get(ATTRIBUTE_VALUE), attributeValue);

        optionSubquery.where(cb.and(optionConfigMatch, optionAttributeNameMatch, optionAttributeValueMatch));

        // Ensure that an OfferOption with this attributeValue exists
        return cb.exists(optionSubquery);
    }

    private static Predicate amenitiesMatch(Subquery<Long> offerSubquery, CriteriaBuilder cb, Root<Offer> offerRoot,
                                            Set<String> amenities) {
        List<Predicate> amenityExistsPredicates = new ArrayList<>();

        for(String amenity : amenities) {
            Predicate amenityExists = amenityExists(offerSubquery, cb, offerRoot, amenity);
            amenityExistsPredicates.add(amenityExists);
        }

        // All amenities specified in the filter must have matching amenities in the offer
        return cb.and(amenityExistsPredicates.toArray(new Predicate[0]));

    }

    private static Predicate amenityExists(Subquery<Long> offerSubquery, CriteriaBuilder cb, Root<Offer> offerRoot, String amenity) {

        Subquery<Long> amenitySubquery = offerSubquery.subquery(Long.class);
        Root<Offer> correlatedOfferRoot = amenitySubquery.correlate(offerRoot);
        Join<Offer, AnimalAmenity> animalAmenityJoin = correlatedOfferRoot.join(ANIMAL_AMENITIES);
        Join<AnimalAmenity, Amenity> amenityJoin = animalAmenityJoin.join(AMENITY, JoinType.INNER);
        amenitySubquery.select(animalAmenityJoin.get(ID));

        Predicate amenityMatch = cb.equal(amenityJoin.get(NAME), amenity);

        amenitySubquery.where(amenityMatch);
        return cb.exists(amenitySubquery);

    }

    private static Predicate availabilityMatchForOffer(Root<Caretaker> root, CriteriaQuery<?> query,
                                                       CriteriaBuilder cb, OfferFilterDTO offerFilter) {

        Subquery<Long> offerSubquery = query.subquery(Long.class);
        Root<Offer> offerRoot = offerSubquery.from(Offer.class);
        offerSubquery.select(offerRoot.get(ID));

        Predicate caretakerMatch = cb.equal(offerRoot.get(CARETAKER), root);
        Predicate animalTypeMatch = cb.equal(offerRoot.get(ANIMAL).get(ANIMAL_TYPE), offerFilter.animalType());

        List<Predicate> availabilityPredicates = new ArrayList<>();
        availabilityPredicates.add(caretakerMatch);
        availabilityPredicates.add(animalTypeMatch);

        if(!offerFilter.availabilities().isEmpty()) {
            availabilityPredicates.add(availabilitiesMatch(cb, offerRoot, offerFilter.availabilities()));
        }

        offerSubquery.where(availabilityPredicates.toArray(new Predicate[0]));

        return cb.exists(offerSubquery);
    }

    private static Predicate availabilitiesMatch(CriteriaBuilder cb, Root<Offer> offerRoot,
                                                 Set<AvailabilityFilterDTO> availabilities) {

        List<Predicate> availabilityPredicates = new ArrayList<>();

        for(AvailabilityFilterDTO availabilityFilter: availabilities) {
            availabilityPredicates.add(availabilityMatch(cb, offerRoot, availabilityFilter));
        }

        return cb.or(availabilityPredicates.toArray(new Predicate[0]));
    }

    private static Predicate availabilityMatch(CriteriaBuilder cb, Root<Offer> offerRoot,
                                               AvailabilityFilterDTO availabilityFilter) {

        Join<Offer, Availability> availabilityJoin = offerRoot.join(AVAILABILITIES, JoinType.INNER);

        Predicate overlapFrom = cb.lessThanOrEqualTo(availabilityJoin.get(AVAILABLE_FROM), availabilityFilter.availableFrom());
        Predicate overlapTo = cb.greaterThanOrEqualTo(availabilityJoin.get(AVAILABLE_TO), availabilityFilter.availableTo());

        return cb.and(overlapFrom, overlapTo);

    }
}

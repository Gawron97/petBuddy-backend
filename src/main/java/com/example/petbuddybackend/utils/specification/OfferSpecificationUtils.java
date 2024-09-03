package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.OfferSearchCriteria;
import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class OfferSpecificationUtils {

    public static final String ANIMAL = "animal";
    public static final String ANIMAL_TYPE = "animalType";
    public static final String ANIMAl_AMENITIES = "animalAmenities";
    public static final String AMENITY = "amenity";
    public static final String AMENITY_NAME = "name";
    public static final String OFFER_CONFIGURATIONS = "offerConfigurations";
    public static final String OFFER_OPTIONS = "offerOptions";
    public static final String ANIMAL_ATTRIBUTE = "animalAttribute";
    public static final String ID = "id";
    public static final String PRICE = "dailyPrice";

    public static Specification<Offer> toSpecification(OfferSearchCriteria filters) {

        Specification<Offer> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction()
        );

        if(CollectionUtil.isNotEmpty(filters.animalTypes())) {
            spec = spec.and(animalTypesIn(filters.animalTypes()));
        }

        if(CollectionUtil.isNotEmpty(filters.amenities())) {
            spec = spec.and(amenitiesIn(filters.amenities()));
        }

        if(CollectionUtil.isNotEmpty(filters.animalAttributeIds())) {
            spec = spec.and(animalAttributeIdsIn(filters.animalAttributeIds(), filters.minPrice(), filters.maxPrice()));
        }

        return spec;

    }

    private static Specification<Offer> animalTypesIn(Set<String> animalTypes) {
        return (root, query, criteriaBuilder) -> {
            Join<Offer, Animal> animalJoin = root.join(ANIMAL);
            return animalJoin.get(ANIMAL_TYPE).in(animalTypes);
        };
    }

    private static Specification<Offer> amenitiesIn(Set<String> amenities) {
        return (root, query, criteriaBuilder) -> {
            Join<Offer, AnimalAmenity> animalAmenityJoin = root.join(ANIMAl_AMENITIES);
            Join<AnimalAmenity, Amenity> amenityJoin = animalAmenityJoin.join(AMENITY);
            return amenityJoin.get(AMENITY_NAME).in(amenities);
        };
    }

    private static Specification<Offer> animalAttributeIdsIn(Set<Long> animalAttributeIds, Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            Join<Offer, OfferConfiguration> offerOfferConfigurationJoin = root.join(OFFER_CONFIGURATIONS);
            Join<OfferConfiguration, OfferOption> offerOptionJoin = offerOfferConfigurationJoin.join(OFFER_OPTIONS);
            Join<OfferOption, AnimalAttribute> animalAttributeJoin = offerOptionJoin.join(ANIMAL_ATTRIBUTE);

            Predicate animalAttributePredicate = animalAttributeJoin.get(ID).in(animalAttributeIds);

            Predicate price = criteriaBuilder.conjunction();

            if(minPrice != null) {
                price = criteriaBuilder.and(price, criteriaBuilder.greaterThanOrEqualTo(offerOfferConfigurationJoin.get(PRICE), minPrice));
            }

            if(maxPrice != null) {
                price = criteriaBuilder.and(price, criteriaBuilder.lessThanOrEqualTo(offerOfferConfigurationJoin.get(PRICE), maxPrice));
            }

            return criteriaBuilder.and(animalAttributePredicate, price);
        };
    }

}

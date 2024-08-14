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
import org.keycloak.common.util.CollectionUtil;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public final class OfferSpecificationUtils {

    public static final String ANIMAL = "animal";
    public static final String ANIMAL_TYPE = "animalType";
    public static final String ANIMAl_AMENITIES = "animalAmenities";
    public static final String AMENITY = "amenity";
    public static final String OFFER_CONFIGURATIONS = "offerConfigurations";
    public static final String OFFER_OPTIONS = "offerOptions";
    public static final String ANIMAL_ATTRIBUTE = "animalAttribute";
    public static final String ID = "id";

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

        if(filters.minPrice() != null) {
            spec = spec.and(minPrice(filters.minPrice()));
        }

        if(filters.maxPrice() != null) {
            spec = spec.and(maxPrice(filters.maxPrice()));
        }

        if(CollectionUtil.isNotEmpty(filters.animalAttributeIds())) {
            spec = spec.and(animalAttributeIdsIn(filters.animalAttributeIds()));
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
            return amenityJoin.get(AMENITY).in(amenities);
        };
    }

    private static Specification<Offer> minPrice(Double minPrice) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<Offer> maxPrice(Double maxPrice) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    private static Specification<Offer> animalAttributeIdsIn(Set<Long> animalAttributeIds) {
        return (root, query, criteriaBuilder) -> {
            Join<Offer, OfferConfiguration> offerOfferConfigurationJoin = root.join(OFFER_CONFIGURATIONS);
            Join<OfferConfiguration, OfferOption> offerOptionJoin = offerOfferConfigurationJoin.join(OFFER_OPTIONS);
            Join<OfferOption, AnimalAttribute> animalAttributeJoin = offerOptionJoin.join(ANIMAL_ATTRIBUTE);
            return animalAttributeJoin.get(ID).in(animalAttributeIds);
        };
    }

}

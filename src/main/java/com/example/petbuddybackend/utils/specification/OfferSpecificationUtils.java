package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.OfferSearchCriteria;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.criteria.Join;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public final class OfferSpecificationUtils {

    public static final String ANIMAL = "animal";
    public static final String ANIMAL_TYPE = "animalType";

    public static Specification<Offer> toSpecification(OfferSearchCriteria filters) {

        Specification<Offer> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction()
        );

        if(CollectionUtil.isNotEmpty(filters.animalTypes())) {
            spec = spec.and(animalTypesIn(filters.animalTypes()));
        }

        return spec;

    }

    private static Specification<Offer> animalTypesIn(Set<String> animalTypes) {
        return (root, query, criteriaBuilder) -> {
            Join<Offer, Animal> animalJoin = root.join(ANIMAL);
            return animalJoin.get(ANIMAL_TYPE).in(animalTypes);
        };
    }

}

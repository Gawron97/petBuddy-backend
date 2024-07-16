package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CaretakerSpecificationUtils {

    public static final String VOIVODESHIP = "voivodeship";
    public static final String ADDRESS = "address";
    public static final String CITY = "city";
    public static final String ACCOUNT_DATA = "accountData";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String ANIMAL_TYPES = "animalsTakenCareOf";
    public static final String EMAIL = "email";


    private CaretakerSpecificationUtils() {
    }

    public static Specification<Caretaker> toSpecification(CaretakerSearchCriteria filters) {
        var spec = Specification.<Caretaker>where(
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

        if (!CollectionUtils.isEmpty(filters.animalTypes())) {
            spec = spec.and(animalTypesIn(filters.animalTypes()));
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

    private static Specification<Caretaker> animalTypesIn(Set<AnimalType> animalTypes) {
        return (root, query, criteriaBuilder) ->
                root.join(ANIMAL_TYPES).in(animalTypes);
    }
}

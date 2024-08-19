package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.OfferSearchCriteria;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
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
    public static final String EMAIL = "email";
    public static final String OFFERS = "offers";
    public static final String CARETAKER = "caretaker";


    private CaretakerSpecificationUtils() {
    }

    public static Specification<Caretaker> toSpecification(CaretakerSearchCriteria filters) {
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

        if(!ObjectUtils.isEmpty(filters.offerSearchCriteria())) {
            spec = spec.and(offerSearchCriteria(filters.offerSearchCriteria()));
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

    private static Specification<Caretaker> offerSearchCriteria(OfferSearchCriteria filters) {
        Specification<Offer> offerSpec = OfferSpecificationUtils.toSpecification(filters);

        return (root, query, criteriaBuilder) -> {
            Subquery<Offer> offerSubquery = query.subquery(Offer.class);
            Root<Offer> offerRoot = offerSubquery.from(Offer.class);

            offerSubquery.select(offerRoot);
            offerSubquery.where(
                    criteriaBuilder.and(
                            criteriaBuilder.equal(offerRoot.get(CARETAKER), root),
                            offerSpec.toPredicate(offerRoot, query, criteriaBuilder)
                    )
            );
            return criteriaBuilder.exists(offerSubquery);
        };

    }

}

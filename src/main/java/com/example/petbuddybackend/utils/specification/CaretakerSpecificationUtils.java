package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.OfferSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferConfigurationFilterDTO;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    public static Specification<Caretaker> toSpecification(CaretakerSearchCriteria filters, List<OfferFilterDTO> offerFilters) {
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

        if(offerFilters != null && !offerFilters.isEmpty()) {
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

    private static Specification<Caretaker> offersMatch(List<OfferFilterDTO> offerFilters) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> animalOfferPredicates = new ArrayList<>();

            for (OfferFilterDTO offerFilter : offerFilters) {
                animalOfferPredicates.add(animalOfferMatches(root, query, cb, offerFilter));
            }

            // The caretaker must have offers for all animals specified in the filters
            return cb.and(animalOfferPredicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate animalOfferMatches(Root<Caretaker> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                                OfferFilterDTO offerFilter) {
        Subquery<Long> offerSubquery = query.subquery(Long.class);
        Root<Offer> offerRoot = offerSubquery.from(Offer.class);
        offerSubquery.select(offerRoot.get("id"));

        Predicate caretakerMatch = cb.equal(offerRoot.get("caretaker"), root);
        Predicate animalTypeMatch = cb.equal(offerRoot.get("animal").get("animalType"), offerFilter.animalType());

        List<Predicate> offerPredicates = new ArrayList<>();
        offerPredicates.add(caretakerMatch);
        offerPredicates.add(animalTypeMatch);

        if (offerFilter.offerConfigurations() != null && !offerFilter.offerConfigurations().isEmpty()) {
            Predicate configurationsMatch = configurationsMatch(offerSubquery, cb, offerRoot, offerFilter.offerConfigurations());
            offerPredicates.add(configurationsMatch);
        }


        offerSubquery.where(offerPredicates.toArray(new Predicate[0]));

        // The caretaker must have an offer for this animal that meets all the criteria
        return cb.exists(offerSubquery);
    }

    private static Predicate configurationsMatch(Subquery<Long> offerSubquery, CriteriaBuilder cb, Root<Offer> offerRoot,
                                                 List<OfferConfigurationFilterDTO> configFilters) {
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
        configSubquery.select(configRoot.get("id"));

        Predicate offerMatch = cb.equal(configRoot.get("offer"), offerRoot);
        Predicate pricePredicate = cb.between(configRoot.get("dailyPrice"), configFilter.minPrice(), configFilter.maxPrice());

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
                                             Root<OfferConfiguration> configRoot, Map<String, List<String>> attributes) {
        List<Predicate> attributeGroupPredicates = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            List<String> attributeValues = entry.getValue();

            Predicate attributeGroupPredicate = attributeGroupMatches(configSubquery, cb, configRoot, attributeName, attributeValues);
            attributeGroupPredicates.add(attributeGroupPredicate);
        }

        return cb.and(attributeGroupPredicates.toArray(new Predicate[0]));
    }

    private static Predicate attributeGroupMatches(Subquery<Long> configSubquery, CriteriaBuilder cb, Root<OfferConfiguration> configRoot, String attributeName, List<String> attributeValues) {
        // First, check if the offer configuration has any OfferOption with this attributeName
        Subquery<Long> attributeNameSubquery = configSubquery.subquery(Long.class);
        Root<OfferOption> attributeNameOptionRoot = attributeNameSubquery.from(OfferOption.class);
        attributeNameSubquery.select(attributeNameOptionRoot.get("id"));

        Predicate configOptionMatch = cb.equal(attributeNameOptionRoot.get("offerConfiguration"), configRoot);
        Predicate attributeNameMatch = cb.equal(attributeNameOptionRoot.get("animalAttribute").get("attributeName"), attributeName);

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
        optionSubquery.select(optionRoot.get("id"));

        Predicate optionConfigMatch = cb.equal(optionRoot.get("offerConfiguration"), configRoot);
        Predicate optionAttributeNameMatch = cb.equal(optionRoot.get("animalAttribute").get("attributeName"), attributeName);
        Predicate optionAttributeValueMatch = cb.equal(optionRoot.get("animalAttribute").get("attributeValue"), attributeValue);

        optionSubquery.where(cb.and(optionConfigMatch, optionAttributeNameMatch, optionAttributeValueMatch));

        // Ensure that an OfferOption with this attributeValue exists
        return cb.exists(optionSubquery);
    }


}

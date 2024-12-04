package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.dto.criteriaSearch.CareStatisticsSearchCriteria;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.criteria.Join;
import lombok.NoArgsConstructor;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;

import static com.example.petbuddybackend.utils.specification.SpecificationCommons.*;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CareSpecificationUtils {

    public static Specification<Care> toSpecificationForCaretaker(CareSearchCriteria filters,
                                                                  Set<String> clientEmails,
                                                                  String caretakerEmail) {

        Specification<Care> spec = toSpecification(filters).and(addCaretakerEmailFilter(caretakerEmail));

        if(CollectionUtil.isNotEmpty(clientEmails)) {
            spec = spec.and(addClientEmailsFilter(clientEmails));
        }

        return spec;

    }

    public static Specification<Care> toSpecificationForClient(CareSearchCriteria filters,
                                                               Set<String> caretakerEmails,
                                                               String clientEmail) {

        Specification<Care> spec = toSpecification(filters)
                        .and(addClientEmailFilter(clientEmail));

        if(CollectionUtil.isNotEmpty(caretakerEmails)) {
            spec = spec.and(addCaretakerEmailsFilter(caretakerEmails));
        }

        return spec;

    }

    public static Specification<Care> toSpecification(CareSearchCriteria filters) {

        Specification<Care> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction()
        );

        if(CollectionUtil.isNotEmpty(filters.animalTypes())) {
            spec = spec.and(animalTypesIn(filters.animalTypes()));
        }

        if(CollectionUtil.isNotEmpty(filters.caretakerStatuses())) {
            spec = spec.and(caretakerStatusesIn(filters.caretakerStatuses()));
        }

        if(CollectionUtil.isNotEmpty(filters.clientStatuses())) {
            spec = spec.and(clientStatusesIn(filters.clientStatuses()));
        }

        if(filters.minCreatedTime() != null) {
            spec = spec.and(minCreatedTime(filters.minCreatedTime()));
        }

        if(filters.maxCreatedTime() != null) {
            spec = spec.and(maxCreatedTime(filters.maxCreatedTime()));
        }

        if(filters.minCareStart() != null) {
            spec = spec.and(minCareStart(filters.minCareStart()));
        }

        if(filters.maxCareStart() != null) {
            spec = spec.and(maxCareStart(filters.maxCareStart()));
        }

        if(filters.minCareEnd() != null) {
            spec = spec.and(minCareEnd(filters.minCareEnd()));
        }

        if(filters.maxCareEnd() != null) {
            spec = spec.and(maxCareEnd(filters.maxCareEnd()));
        }

        if(filters.minDailyPrice() != null) {
            spec = spec.and(minDailyPrice(filters.minDailyPrice()));
        }

        if(filters.maxDailyPrice() != null) {
            spec = spec.and(maxDailyPrice(filters.maxDailyPrice()));
        }

        return spec;

    }

    public static Specification<Care> toSpecificationForCaretaker(CareStatisticsSearchCriteria filters,
                                                                  Set<String> clientEmails,
                                                                  Set<CareStatus> caretakerStatuses,
                                                                  Set<CareStatus> clientStatuses,
                                                                  String caretakerEmail) {

        Specification<Care> spec = toSpecification(filters, caretakerStatuses, clientStatuses)
                .and(addCaretakerEmailFilter(caretakerEmail));

        if(CollectionUtil.isNotEmpty(clientEmails)) {
            spec = spec.and(addClientEmailsFilter(clientEmails));
        }

        return spec;

    }

    public static Specification<Care> toSpecification(CareStatisticsSearchCriteria filters,
                                                      Set<CareStatus> caretakerStatuses,
                                                      Set<CareStatus> clientStatuses) {

        Specification<Care> spec = Specification.where(
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction()
        );

        if(CollectionUtil.isNotEmpty(filters.animalTypes())) {
            spec = spec.and(animalTypesIn(filters.animalTypes()));
        }

        if(CollectionUtil.isNotEmpty(caretakerStatuses)) {
            spec = spec.and(caretakerStatusesIn(caretakerStatuses));
        }

        if(CollectionUtil.isNotEmpty(clientStatuses)) {
            spec = spec.and(clientStatusesIn(clientStatuses));
        }

        if(filters.minCareStart() != null) {
            spec = spec.and(minCareStart(filters.minCareStart()));
        }

        if(filters.maxCareStart() != null) {
            spec = spec.and(maxCareStart(filters.maxCareStart()));
        }

        if(filters.minDailyPrice() != null) {
            spec = spec.and(minDailyPrice(filters.minDailyPrice()));
        }

        if(filters.maxDailyPrice() != null) {
            spec = spec.and(maxDailyPrice(filters.maxDailyPrice()));
        }

        return spec;

    }

    private static Specification<Care> animalTypesIn(Set<String> animalTypes) {
        return (root, query, criteriaBuilder) -> {
            Join<Care, Animal> animalJoin = root.join(ANIMAL);
            return animalJoin.get(ANIMAL_TYPE).in(animalTypes);
        };
    }

    private static Specification<Care> caretakerStatusesIn(Set<CareStatus> caretakerStatuses) {
        return (root, query, criteriaBuilder) ->
                root.get(CARETAKER_STATUS).in(caretakerStatuses);
    }

    private static Specification<Care> clientStatusesIn(Set<CareStatus> clientStatuses) {
        return (root, query, criteriaBuilder) ->
                root.get(CLIENT_STATUS).in(clientStatuses);
    }

    private static Specification<Care> minCreatedTime(ZonedDateTime minCreatedTime) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(SUBMITTED_AT), minCreatedTime);
    }

    private static Specification<Care> maxCreatedTime(ZonedDateTime maxCreatedTime) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(SUBMITTED_AT), maxCreatedTime);
    }

    private static Specification<Care> minCareStart(LocalDate minCareStart) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(CARE_START), minCareStart);
    }

    private static Specification<Care> maxCareStart(LocalDate maxCareStart) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(CARE_START), maxCareStart);
    }

    private static Specification<Care> minCareEnd(LocalDate minCareEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(CARE_END), minCareEnd);
    }

    private static Specification<Care> maxCareEnd(LocalDate maxCareEnd) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(CARE_END), maxCareEnd);
    }

    private static Specification<Care> minDailyPrice(BigDecimal minDailyPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(DAILY_PRICE), minDailyPrice);
    }

    private static Specification<Care> maxDailyPrice(BigDecimal maxDailyPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(DAILY_PRICE), maxDailyPrice);
    }

    public static Specification<Care> addClientEmailsFilter(Set<String> clientEmails) {
        return (root, query, criteriaBuilder) -> {
            Join<Care, Client> clientJoin = root.join(CLIENT);
            return clientJoin.get(EMAIL).in(clientEmails);
        };
    }

    public static Specification<Care> addCaretakerEmailsFilter(Set<String> caretakerEmails) {
        return (root, query, criteriaBuilder) -> {
            Join<Care, Caretaker> caretakerJoin = root.join(CARETAKER);
            return caretakerJoin.get(EMAIL).in(caretakerEmails);
        };
    }

    public static Specification<Care> addCaretakerEmailFilter(String caretakerEmail) {
        return (root, query, criteriaBuilder) -> {
            Join<Care, Caretaker> caretakerJoin = root.join(CARETAKER);
            return criteriaBuilder.equal(caretakerJoin.get(EMAIL), caretakerEmail);
        };
    }

    public static Specification<Care> addClientEmailFilter(String clientEmail) {
        return (root, query, criteriaBuilder) -> {
            Join<Care, Client> clientJoin = root.join(CLIENT);
            return criteriaBuilder.equal(clientJoin.get(EMAIL), clientEmail);
        };
    }


}

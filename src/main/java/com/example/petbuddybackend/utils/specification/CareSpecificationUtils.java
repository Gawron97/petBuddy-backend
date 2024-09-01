package com.example.petbuddybackend.utils.specification;

import com.example.petbuddybackend.dto.criteriaSearch.CareSearchCriteria;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.criteria.Join;
import lombok.NoArgsConstructor;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CareSpecificationUtils {

    public static final String CLIENT = "client";
    public static final String CARETAKER = "caretaker";
    public static final String EMAIL = "email";
    public static final String ANIMAL = "animal";
    public static final String ANIMAL_TYPE = "animalType";
    public static final String CARETAKER_STATUS = "caretakerStatus";
    public static final String CLIENT_STATUS = "clientStatus";


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

        return spec;

    }

    private static Specification<Care> animalTypesIn(Set<String> animalTypes) {
        return (root, query, criteriaBuilder) -> {
            Join<Care, Animal> animalJoin = root.join(ANIMAL);
            return animalJoin.get(ANIMAL_TYPE).in(animalTypes);
        };
    }

    private static Specification<Care> caretakerStatusesIn(Set<CareStatus> caretakerStatuses) {

        return (root, query, criteriaBuilder) -> {
            return root.get(CARETAKER_STATUS).in(caretakerStatuses);
        };

    }

    private static Specification<Care> clientStatusesIn(Set<CareStatus> clientStatuses) {
        return (root, query, criteriaBuilder) -> {
            return root.get(CLIENT_STATUS).in(clientStatuses);
        };
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

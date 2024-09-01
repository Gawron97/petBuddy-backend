package com.example.petbuddybackend.dto.criteriaSearch;

import com.example.petbuddybackend.entity.care.CareStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record CareSearchCriteria(
        @Schema(
                description = "Filters by animal types. Results consists of caretakers that can take care of any animal from the list",
                examples = {"DOG", "CAT", "BIRD", "SMALL_PET", "FISH", "REPTILE", "HORSE"},
                type = "array"
        )
        Set<String> animalTypes,

        @Schema(
                description = "Filters by caretaker statuses",
                examples = {"PENDING", "ACCEPTED", "AWAITING_PAYMENT", "PAID", "CANCELLED", "OUTDATED"},
                type = "array"
        )
        Set<CareStatus> caretakerStatuses,

        @Schema(
                description = "Filters by client statuses",
                examples = {"PENDING", "ACCEPTED", "AWAITING_PAYMENT", "PAID", "CANCELLED", "OUTDATED"},
                type = "array"
        )
        Set<CareStatus> clientStatuses
) {
}

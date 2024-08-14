package com.example.petbuddybackend.dto.criteriaSearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
public record OfferSearchCriteria(
        @Schema(
                description = "Filters by animal types. Results consists of caretakers that can take care of any animal from the list",
                examples = {"DOG", "CAT", "BIRD", "SMALL_PET", "FISH", "REPTILE", "HORSE"},
                type = "array"
        )
        Set<String> animalTypes
) {
}

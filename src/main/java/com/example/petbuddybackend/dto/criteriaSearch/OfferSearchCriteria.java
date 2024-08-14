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
        Set<String> animalTypes,

        @Schema(
                description = "Filters by amenities. Results consists of caretakers that have amenities from the list",
                examples = {"scratching post", "toys"},
                type = "array"
        )
        Set<String> amenities,

        @Schema(description = "Filters by min price")
        Double minPrice,

        @Schema(description = "Filters by max price")
        Double maxPrice,

        @Schema(
                description = "Filters by attribute values. Results consists of caretakers that have attributes value from the list",
                type = "array"
        )
        Set<Long> animalAttributeIds
) {
}

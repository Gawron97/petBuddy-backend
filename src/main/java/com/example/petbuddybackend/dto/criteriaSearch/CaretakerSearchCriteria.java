package com.example.petbuddybackend.dto.criteriaSearch;

import com.example.petbuddybackend.entity.address.Voivodeship;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
public record CaretakerSearchCriteria(
        @Schema(description = "Filters by data like surname, name, email")
        String personalDataLike,

        @Schema(description = "Filters by city")
        String cityLike,

        @Schema(
                description = "Filters by voivodeship",
                allowableValues = {
                        "DOLNOSLASKIE", "KUJAWSKO_POMORSKIE", "LUBELSKIE", "LUBUSKIE",
                        "LODZKIE", "MALOPOLSKIE", "MAZOWIECKIE", "OPOLSKIE", "PODKARPACKIE",
                        "PODLASKIE", "POMORSKIE", "SLASKIE", "SWIETOKRZYSKIE", "WARMINSKO_MAZURSKIE",
                        "WIELKOPOLSKIE", "ZACHODNIOPOMORSKIE"
                })
        Voivodeship voivodeship
) {
}

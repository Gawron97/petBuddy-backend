package com.example.petbuddybackend.dto.criteriaSearch;

import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import com.example.petbuddybackend.utils.annotation.validation.DateRangeField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Set;

@Builder
@DateRange(fields = {
        @DateRangeField(startDateField = "minCareStart", endDateField = "maxCareStart")
}, message = "Max date should be greater than min date")
public record CareStatisticsSearchCriteria(
        @Schema(
                description = "Filters by animal types in cares",
                allowableValues = {"DOG", "CAT", "BIRD", "FISH", "REPTILE", "HORSE"},
                type = "array"
        )
        Set<String> animalTypes,

        @Schema(
                description = "Filters by min care start - NOT PRESENT DATE, ONLY YEAR AND MONTH",
                example = "2020-01",
                type = "string"
        )
        @PastOrPresent(message = "Care start date must be before or equal to today")
        YearMonth minCareStart,

        @Schema(
                description = "Filters by max care start - NOT PRESENT DATE, ONLY YEAR AND MONTH",
                example = "2020-02",
                type = "string"
        )
        @PastOrPresent(message = "Care start date must be before or equal to today")
        YearMonth maxCareStart,

        @Schema(
                description = "Filters by min daily price",
                example = "10.00",
                type = "number"
        ) BigDecimal minDailyPrice,

        @Schema(
                description = "Filters by max daily price",
                example = "30.00",
                type = "number"
        )
        BigDecimal maxDailyPrice
) {}

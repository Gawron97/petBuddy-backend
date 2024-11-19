package com.example.petbuddybackend.dto.criteriaSearch;

import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import com.example.petbuddybackend.utils.annotation.validation.DateRangeField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;

@Builder
@DateRange(fields = {
        @DateRangeField(startDateField = "minCreatedTime", endDateField = "maxCreatedTime"),
        @DateRangeField(startDateField = "minCareStart", endDateField = "maxCareStart"),
        @DateRangeField(startDateField = "minCareEnd", endDateField = "maxCareEnd")
}, message = "Max created time must be after or equal to min created time")
public record CareSearchCriteria(
        @Schema(
                description = "Filters by animal types in cares",
                allowableValues = {"DOG", "CAT", "BIRD", "FISH", "REPTILE", "HORSE"},
                type = "array"
        )
        Set<String> animalTypes,

        @Schema(
                description = "Filters by caretaker statuses",
                allowableValues = {"PENDING", "ACCEPTED", "READY_TO_PROCEED", "CONFIRMED", "CANCELLED", "OUTDATED"},
                type = "array"
        )
        Set<CareStatus> caretakerStatuses,

        @Schema(
                description = "Filters by client statuses",
                allowableValues = {"PENDING", "ACCEPTED", "READY_TO_PROCEED", "CONFIRMED", "CANCELLED", "OUTDATED"},
                type = "array"
        )
        Set<CareStatus> clientStatuses,

        @Schema(
                description = "Filters by min created time",
                example = "2020-01-03 12:00:05.123 +0100",
                type = "string"
        )
        ZonedDateTime minCreatedTime,

        @Schema(
                description = "Filters by max created time",
                example = "2020-01-07 12:05:05.123 +0100",
                type = "string"
        )
        ZonedDateTime maxCreatedTime,

        @Schema(
                description = "Filters by min care start",
                example = "2020-02-03",
                type = "string"
        )
        LocalDate minCareStart,

        @Schema(
                description = "Filters by max care start",
                example = "2020-02-07",
                type = "string"
        )
        LocalDate maxCareStart,

        @Schema(
                description = "Filters by min care end",
                example = "2020-02-08",
                type = "string"
        )
        LocalDate minCareEnd,

        @Schema(
                description = "Filters by max care end",
                example = "2020-02-17",
                type = "string"
        )
        LocalDate maxCareEnd,

        @Schema(
                description = "Filters by min daily price",
                example = "10.00",
                type = "number"
        )
        BigDecimal minDailyPrice,

        @Schema(
                description = "Filters by max daily price",
                example = "30.00",
                type = "number"
        )
        BigDecimal maxDailyPrice
) {
}

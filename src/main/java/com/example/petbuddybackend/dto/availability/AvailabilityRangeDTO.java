package com.example.petbuddybackend.dto.availability;

import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import com.example.petbuddybackend.utils.annotation.validation.DateRangeField;
import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@DateRange(fields = {
        @DateRangeField(startDateField = "availableFrom", endDateField = "availableTo")
}, message = "Available from must be before or equal to available to")
public record AvailabilityRangeDTO(
        @Future
        @JsonFormat(pattern = TimeUtils.DATE_FORMAT)
        @NotNull
        LocalDate availableFrom,

        @Future
        @JsonFormat(pattern = TimeUtils.DATE_FORMAT)
        @NotNull
        LocalDate availableTo
) {
        public boolean overlaps(AvailabilityRangeDTO availabilityRangeDTO) {
                return this.availableFrom.isBefore(availabilityRangeDTO.availableTo) &&
                        this.availableTo.isAfter(availabilityRangeDTO.availableFrom);
        }
}

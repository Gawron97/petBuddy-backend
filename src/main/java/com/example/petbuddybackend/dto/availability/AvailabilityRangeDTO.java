package com.example.petbuddybackend.dto.availability;

import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import com.example.petbuddybackend.utils.annotation.validation.DateRangeField;
import jakarta.validation.constraints.Future;
import lombok.ToString;

import java.time.ZonedDateTime;

@DateRange(fields = {
        @DateRangeField(startDateField = "availableFrom", endDateField = "availableTo")
}, message = "Available from must be before available to")
public record AvailabilityRangeDTO(
        @Future
        ZonedDateTime availableFrom,

        @Future
        ZonedDateTime availableTo
) {
        public boolean overlaps(AvailabilityRangeDTO availabilityRangeDTO) {
                return this.availableFrom.isBefore(availabilityRangeDTO.availableTo) &&
                        this.availableTo.isAfter(availabilityRangeDTO.availableFrom);
        }
}

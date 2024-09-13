package com.example.petbuddybackend.dto.availability;

import com.example.petbuddybackend.utils.annotation.validation.DateRange;
import com.example.petbuddybackend.utils.annotation.validation.DateRangeField;
import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import lombok.Builder;
import lombok.ToString;

import java.time.ZonedDateTime;

@Builder
@DateRange(fields = {
        @DateRangeField(startDateField = "availableFrom", endDateField = "availableTo")
}, message = "Available from must be before available to")
public record AvailabilityRangeDTO(
        @Future
        @JsonFormat(pattern = TimeUtils.ZONED_DATETIME_FORMAT)
        ZonedDateTime availableFrom,

        @Future
        @JsonFormat(pattern = TimeUtils.ZONED_DATETIME_FORMAT)
        ZonedDateTime availableTo
) {
        public boolean overlaps(AvailabilityRangeDTO availabilityRangeDTO) {
                return this.availableFrom.isBefore(availabilityRangeDTO.availableTo) &&
                        this.availableTo.isAfter(availabilityRangeDTO.availableFrom);
        }
}

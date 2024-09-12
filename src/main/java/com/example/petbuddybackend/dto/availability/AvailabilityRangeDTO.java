package com.example.petbuddybackend.dto.availability;

import java.time.ZonedDateTime;

public record AvailabilityRangeDTO(
        ZonedDateTime availableFrom,
        ZonedDateTime availableTo
) {
}

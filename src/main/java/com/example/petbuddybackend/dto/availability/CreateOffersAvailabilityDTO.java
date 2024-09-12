package com.example.petbuddybackend.dto.availability;

import java.util.List;

public record CreateOffersAvailabilityDTO(
        List<Long> offerIds,
        List<AvailabilityRangeDTO> availabilityRanges
) {
}

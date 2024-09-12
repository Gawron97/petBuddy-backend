package com.example.petbuddybackend.dto.availability;

import java.util.List;

public record CreateOffersAvailabilityDTO(
        List<String> animalTypes,
        List<AvailabilityRangeDTO> availabilityRanges
) {
}

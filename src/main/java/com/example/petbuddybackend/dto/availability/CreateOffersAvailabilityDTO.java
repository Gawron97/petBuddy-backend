package com.example.petbuddybackend.dto.availability;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOffersAvailabilityDTO(
        @NotEmpty
        List<Long> offerIds,

        @NotEmpty
        List<AvailabilityRangeDTO> availabilityRanges
) {
}

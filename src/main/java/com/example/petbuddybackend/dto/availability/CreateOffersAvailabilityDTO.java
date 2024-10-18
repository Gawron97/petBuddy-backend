package com.example.petbuddybackend.dto.availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record CreateOffersAvailabilityDTO(
        @NotEmpty
        List<Long> offerIds,

        Set<@Valid AvailabilityRangeDTO> availabilityRanges
) {
}

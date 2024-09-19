package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record CaretakerDTO(
        AccountDataDTO accountData,
        AddressDTO address,
        List<String> animals,
        Integer numberOfRatings,
        Float avgRating,
        Integer availabilityDaysMatch
) {
}

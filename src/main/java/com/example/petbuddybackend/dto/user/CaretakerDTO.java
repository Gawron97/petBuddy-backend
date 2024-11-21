package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.PublicAddressDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record CaretakerDTO(
        AccountDataDTO accountData,
        PublicAddressDTO address,
        List<String> animals,
        Integer numberOfRatings,
        Float avgRating,
        Float ratingScore
) {
}

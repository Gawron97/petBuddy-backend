package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.PublicAddressDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record CaretakerComplexPublicDTO(
        AccountDataDTO accountData,
        String phoneNumber,
        String description,
        PublicAddressDTO address,
        List<String> animals,
        List<OfferDTO> offers,
        Integer numberOfRatings,
        Float avgRating,
        Float ratingScore
) {
}

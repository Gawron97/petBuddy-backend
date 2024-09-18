package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import lombok.Builder;
import java.util.List;

@Builder
public record CaretakerComplexInfoDTO(
    AccountDataDTO accountData,
    String phoneNumber,
    String description,
    AddressDTO address,
    List<String> animals,
    List<OfferDTO> offers,
    Integer numberOfRatings,
    Float avgRating
){}

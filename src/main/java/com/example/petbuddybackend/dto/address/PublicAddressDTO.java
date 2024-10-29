package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.Voivodeship;
import lombok.Builder;

@Builder
public record PublicAddressDTO(
        String city,
        String zipCode,
        Voivodeship voivodeship,
        String street
) {
}

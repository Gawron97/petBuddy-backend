package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.Voivodeship;
import lombok.Builder;

@Builder
public record AddressDTO(
    Long id,
    String city,
    String zipCode,
    Voivodeship voivodeship,
    String street,
    String buildingNumber,
    String apartmentNumber
) {
}

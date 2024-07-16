package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.Voivodeship;

public record AddressDTO(
    Long id,
    String city,
    String postalCode,
    Voivodeship voivodeship,
    String street,
    String buildingNumber,
    String apartmentNumber
) {
}

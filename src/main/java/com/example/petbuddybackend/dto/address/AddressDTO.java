package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.PolishVoivodeship;

public record AddressDTO(
    Long id,
    String city,
    String postalCode,
    PolishVoivodeship voivodeship,
    String street,
    String buildingNumber,
    String apartmentNumber
) {
}

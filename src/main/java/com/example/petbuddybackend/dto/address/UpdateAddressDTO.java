package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.Voivodeship;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateAddressDTO(
    String city,
    String zipCode,
    Voivodeship voivodeship,
    String street,
    String buildingNumber,
    String apartmentNumber
) {
}

package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.Voivodeship;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AddressDTO(
    Long id,
    @NotNull String city,
    @NotNull String zipCode,
    @NotNull Voivodeship voivodeship,
    @NotNull String street,
    @NotNull String buildingNumber,
    String apartmentNumber
) {
}

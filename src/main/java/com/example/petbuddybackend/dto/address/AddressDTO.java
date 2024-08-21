package com.example.petbuddybackend.dto.address;

import com.example.petbuddybackend.entity.address.Voivodeship;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AddressDTO(
    @NotBlank String city,
    @NotBlank String zipCode,
    @NotNull Voivodeship voivodeship,
    @NotBlank String street,
    @NotBlank String buildingNumber,
    String apartmentNumber
) {
}

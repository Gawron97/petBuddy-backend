package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import lombok.Builder;

@Builder
public record CaretakerDTO(
        String email,
        String name,
        String surname,
        String phoneNumber,
        String description,
        AddressDTO address
) {
}

package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import lombok.Builder;

@Builder
public record CreateCaretakerDTO(
        String phoneNumber,
        String description,
        AddressDTO address
) {
}

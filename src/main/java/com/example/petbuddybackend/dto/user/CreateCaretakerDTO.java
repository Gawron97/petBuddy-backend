package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;

public record CreateCaretakerDTO(
        String phoneNumber,
        String description,
        AddressDTO address
) {
}

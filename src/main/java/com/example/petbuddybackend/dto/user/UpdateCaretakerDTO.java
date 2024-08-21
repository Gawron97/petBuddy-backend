package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.address.UpdateAddressDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateCaretakerDTO(
        String phoneNumber,
        String description,
        UpdateAddressDTO address
) {
}

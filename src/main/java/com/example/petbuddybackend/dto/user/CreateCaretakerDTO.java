package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateCaretakerDTO(
        @NotBlank String phoneNumber,
        String description,
        @NotNull AddressDTO address
) {
}
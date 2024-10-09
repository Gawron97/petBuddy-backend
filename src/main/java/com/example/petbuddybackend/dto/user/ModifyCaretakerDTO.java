package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record ModifyCaretakerDTO(
        @NotBlank String phoneNumber,
        String description,
        @NotNull AddressDTO address,
        Set<String> offerBlobsToKeep
) {
}

package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record ClientDTO(
        AccountDataDTO accountData
) {
}

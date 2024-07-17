package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record AccountDataDTO(
        String email,
        String name,
        String surname
) {
}

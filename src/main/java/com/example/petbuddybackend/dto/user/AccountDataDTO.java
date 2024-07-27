package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record AccountDataDTO(
        Long id,
        String email,
        String name,
        String surname,
        String username
) {
}

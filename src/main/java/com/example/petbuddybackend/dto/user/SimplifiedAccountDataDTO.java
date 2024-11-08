package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record SimplifiedAccountDataDTO(
        String email,
        String name,
        String surname
) {
}

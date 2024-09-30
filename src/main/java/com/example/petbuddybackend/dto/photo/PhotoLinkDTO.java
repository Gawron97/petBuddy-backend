package com.example.petbuddybackend.dto.photo;

import lombok.Builder;

@Builder
public record PhotoLinkDTO(
        String blob,
        String url
) {
}

package com.example.petbuddybackend.dto.user;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import lombok.Builder;

@Builder
public record AccountDataDTO(
        String email,
        String name,
        String surname,
        PhotoLinkDTO profilePicture
) {
}

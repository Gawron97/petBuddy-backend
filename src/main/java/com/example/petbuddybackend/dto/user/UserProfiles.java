package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record UserProfiles(
        String email,
        Boolean hasClientProfile,
        Boolean hasCaretakerProfile
) {
}

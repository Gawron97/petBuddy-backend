package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record UserProfilesData(
        AccountDataDTO accountData,
        Boolean hasClientProfile,
        Boolean hasCaretakerProfile
) {
}

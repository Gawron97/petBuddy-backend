package com.example.petbuddybackend.dto.user;

import lombok.Builder;

@Builder
public record ProfileData(
        AccountDataDTO accountData,
        Boolean hasClientProfile,
        Boolean hasCaretakerProfile
) {
}

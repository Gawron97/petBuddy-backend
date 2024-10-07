package com.example.petbuddybackend.dto.user;

import lombok.Builder;

import java.util.Set;

@Builder
public record ClientComplexInfoDTO(
        AccountDataDTO accountData,
        Set<String> followingCaretakersEmails
) {
}

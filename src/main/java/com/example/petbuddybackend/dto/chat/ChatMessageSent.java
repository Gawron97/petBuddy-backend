package com.example.petbuddybackend.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageSent(@NotBlank String content) {
}

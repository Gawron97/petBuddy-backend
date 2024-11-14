package com.example.petbuddybackend.dto.user;

import lombok.Builder;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Builder
public record SearchCaretakersResponseDTO(
        Page<CaretakerDTO> caretakers,
        BigDecimal cityLatitude,
        BigDecimal cityLongitude
) {
}

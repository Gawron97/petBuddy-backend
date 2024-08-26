package com.example.petbuddybackend.dto.care;

import com.example.petbuddybackend.entity.care.CareStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public record CareDTO(
        Long id,
        ZonedDateTime submittedAt,
        CareStatus caretakerStatus,
        CareStatus clientStatus,
        LocalDate careStart,
        LocalDate careEnd,
        String description,
        BigDecimal dailyPrice,
        String animalType,
        Map<String, List<String>> selectedOptions,
        String caretakerEmail,
        String clientEmail
) {
}

package com.example.petbuddybackend.dto.care;

import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public record CareDTO(
        Long id,
        @JsonFormat(pattern = TimeUtils.ZONED_TIMESTAMP_FORMAT)
        ZonedDateTime submittedAt,
        CareStatus caretakerStatus,
        CareStatus clientStatus,
        @JsonFormat(pattern = TimeUtils.DATE_FORMAT)
        LocalDate careStart,
        @JsonFormat(pattern = TimeUtils.DATE_FORMAT)
        LocalDate careEnd,
        String description,
        BigDecimal dailyPrice,
        String animalType,
        Map<String, List<String>> selectedOptions,
        String caretakerEmail,
        String clientEmail
) {
}

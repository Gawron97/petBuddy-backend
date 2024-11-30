package com.example.petbuddybackend.dto.criteriaSearch;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRoomSearchCriteria(
        @Schema(description = "Filters by data like surname, name, email")
        String chatterDataLike
) {
}

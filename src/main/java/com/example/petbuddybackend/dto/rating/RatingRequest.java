package com.example.petbuddybackend.dto.rating;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record RatingRequest(
        @NotNull @Range(min = 1, max = 5)
        Integer rating,
        @NotNull
        String comment
) {
}

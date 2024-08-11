package com.example.petbuddybackend.dto.rating;

public record RatingResponse(
        String clientEmail,
        String caretakerEmail,
        Integer rating,
        String comment
) {
}

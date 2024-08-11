package com.example.petbuddybackend.dto.rating;

public record RatingResponse(
        String ratingClientEmail,
        String ratedCaretakerEmail,
        Integer rating,
        String comment
) {
}

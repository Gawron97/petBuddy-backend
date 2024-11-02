package com.example.petbuddybackend.dto.rating;

import com.example.petbuddybackend.dto.user.ClientDTO;

public record RatingResponse(
        ClientDTO client,
        String caretakerEmail,
        Integer rating,
        String comment
) {
}

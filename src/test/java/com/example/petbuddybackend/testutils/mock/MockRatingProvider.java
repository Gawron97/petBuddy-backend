package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.rating.Rating;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MockRatingProvider {

    public static Rating createMockRating(Care care) {
        return Rating.builder()
                .careId(care.getId())
                .care(care)
                .rating(5)
                .comment("comment")
                .build();
    }

    public static Rating createMockRating(Care care, Integer rating, String comment) {
        return Rating.builder()
                .careId(care.getId())
                .care(care)
                .rating(rating)
                .comment(comment)
                .build();
    }
}

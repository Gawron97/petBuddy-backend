package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MockRatingProvider {

    public static Rating createMockRating(Caretaker caretaker, Client client, Care care) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .careId(care.getId())
                .caretaker(caretaker)
                .client(client)
                .care(care)
                .rating(5)
                .comment("comment")
                .build();
    }

    public static Rating createMockRating(Caretaker caretaker, Client client, Care care, Integer rating, String comment) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .careId(care.getId())
                .caretaker(caretaker)
                .client(client)
                .care(care)
                .rating(rating)
                .comment(comment)
                .build();
    }

    public static Rating createMockRatingForCaretaker(Caretaker caretaker, Client client, Care care, Integer ratingNumber, String comment) {
        Rating rating = createMockRating(caretaker, client, care, ratingNumber, comment);
        caretaker.getRatings().add(rating);

        client.getRatings().add(rating);

        return rating;
    }

}

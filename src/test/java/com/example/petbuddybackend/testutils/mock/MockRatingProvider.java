package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MockRatingProvider {

    public static Rating createMockRating(Caretaker caretaker, Client client) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .caretaker(caretaker)
                .client(client)
                .rating(5)
                .comment("comment")
                .build();
    }

    public static Rating createMockRating(Caretaker caretaker, Client client, Integer rating, String comment) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .caretaker(caretaker)
                .client(client)
                .rating(rating)
                .comment(comment)
                .build();
    }

    public static Rating createMockRatingForCaretaker(Caretaker caretaker, Client client, Integer ratingNumber, String comment) {
        Rating rating = createMockRating(caretaker, client, ratingNumber, comment);
        caretaker.getRatings().add(rating);

        client.getRatings().add(rating);

        return rating;
    }

}

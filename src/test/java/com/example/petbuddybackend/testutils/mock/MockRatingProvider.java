package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;
import org.keycloak.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

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
        if(CollectionUtil.isNotEmpty(caretaker.getRatings())) {
            caretaker.getRatings().add(rating);
        } else {
            caretaker.setRatings(new ArrayList<>(List.of(rating)));
        }

        if(CollectionUtil.isNotEmpty(client.getRatings())) {
            client.getRatings().add(rating);
        } else {
            client.setRatings(new ArrayList<>(List.of(rating)));
        }

        return rating;
    }

}

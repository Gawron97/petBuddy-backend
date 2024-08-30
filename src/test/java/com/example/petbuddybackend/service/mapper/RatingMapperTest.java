package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockRatingProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RatingMapperTest {

    private final RatingMapper mapper = RatingMapper.INSTANCE;


    @Test
    void mapToCaretakerDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        Client client = MockUserProvider.createMockClient();
        Caretaker caretaker = MockUserProvider.createMockCaretaker();

        Rating rating = MockRatingProvider.createMockRating(caretaker, client);

        RatingResponse ratingResponse = mapper.mapToRatingResponse(rating);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(ratingResponse));
    }
}

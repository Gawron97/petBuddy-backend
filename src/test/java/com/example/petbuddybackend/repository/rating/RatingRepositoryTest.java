package com.example.petbuddybackend.repository.rating;

import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockRatingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Caretaker caretaker;
    private Client client;


    @BeforeEach
    void setUp() {
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
    }

    @AfterEach
    void tearDown() {
        appUserRepository.deleteAll();
    }

    @Test
    void testFindAllByCaretakerEmail_shouldReturnRatingsWithNoNulls() throws IllegalAccessException {
        ratingRepository.saveAndFlush(MockRatingProvider.createMockRating(caretaker, client));

        Page<Rating> ratings = ratingRepository.findAllByCaretakerEmail(caretaker.getEmail(), PageRequest.of(0, 10));
        Rating rating = ratings.getContent().get(0);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(rating));
    }
}

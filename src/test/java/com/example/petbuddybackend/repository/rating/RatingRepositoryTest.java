package com.example.petbuddybackend.repository.rating;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
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
import org.springframework.test.context.ContextConfiguration;

import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CareRepository careRepository;

    @Autowired
    private AnimalRepository animalRepository;

    private Caretaker caretaker;
    private Client client;
    private Care care;


    @BeforeEach
    void setUp() {
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        care = PersistenceUtils.addCare(careRepository, createMockCare(caretaker, client, animalRepository.findById("DOG").get()));
    }

    @AfterEach
    void tearDown() {
        ratingRepository.deleteAll();
        careRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testFindAllByCaretakerEmail_shouldReturnRatingsWithNoNulls() {
        ratingRepository.saveAndFlush(MockRatingProvider.createMockRating(care));

        Page<Rating> ratings = ratingRepository.findAllByCare_Caretaker_Email(caretaker.getEmail(), PageRequest.of(0, 10));
        Rating rating = ratings.getContent().get(0);

        assertTrue(ValidationUtils.fieldsNotNullRecursive(rating));
    }
}

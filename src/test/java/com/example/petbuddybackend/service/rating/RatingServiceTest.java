package com.example.petbuddybackend.service.rating;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testconfig.TestDataConfiguration;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockPaidCare;
import static com.example.petbuddybackend.testutils.mock.MockRatingProvider.createMockRating;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = TestDataConfiguration.class)
public class RatingServiceTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CareRepository careRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Caretaker caretaker;
    private Client client;
    private Client clientSameAsCaretaker;
    private Care care;
    private Care paidCare;


    @BeforeEach
    void init() {
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);
        clientSameAsCaretaker = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                Client.builder()
                    .email(caretaker.getEmail())
                    .accountData(caretaker.getAccountData())
                    .build()
        );
        care = PersistenceUtils.addCare(
                careRepository, createMockCare(caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );
        paidCare = PersistenceUtils.addCare(
                careRepository, createMockPaidCare(caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );
    }

    @AfterEach
    void cleanUp() {
        ratingRepository.deleteAll();
        appUserRepository.deleteAll();
        careRepository.deleteAll();
    }


    @Test
    void testGetRating_sortingParamsShouldAlignWithDTO() {
        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(RatingResponse.class);

        for(String fieldName : fieldNames) {
            assertDoesNotThrow(() -> ratingService.getRatings(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    caretaker.getEmail()
            ));
        }
    }

    @Test
    void rateCaretaker_shouldSucceed() throws IllegalAccessException {
        transactionTemplate.execute(status -> {
            ratingService.rateCaretaker(
                    caretaker.getEmail(),
                    client.getEmail(),
                    paidCare.getId(),
                    5,
                    "comment"
            );
            return null;
        });

        transactionTemplate.execute(status -> {
            Rating rating = ratingRepository.getReferenceById(new RatingKey(caretaker.getEmail(), client.getEmail(), paidCare.getId()));
            assertEquals(1, ratingRepository.count());
            assertEquals(5, rating.getRating());
            assertTrue(ValidationUtils.fieldsNotNullRecursive(rating, Set.of("client", "caretaker", "care")));
            return null;
        });

    }

    @Test
    void rateCaretaker_ratingExists_shouldUpdateRating() {
        transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(caretaker, client, paidCare))
        );

        transactionTemplate.execute(status -> {
            ratingService.rateCaretaker(
                    caretaker.getEmail(),
                    client.getAccountData().getEmail(),
                    paidCare.getId(),
                    5,
                    "new comment"
            );
            return null;
        });

        transactionTemplate.execute(status -> {
            assertEquals(1, ratingRepository.count());
            return null;
        });

        transactionTemplate.execute(status -> {
            Rating rating = ratingRepository.getReferenceById(new RatingKey(caretaker.getEmail(), client.getEmail(), paidCare.getId()));
            assertEquals(5, rating.getRating());
            assertEquals("new comment", rating.getComment());
            assertEquals(client.getEmail(), rating.getClientEmail());
            assertEquals(caretaker.getEmail(), rating.getCaretakerEmail());
            return null;
        });
    }

    @ParameterizedTest
    @MethodSource("provideRatingParams")
    void rateCaretaker_invalidRating_(int rating, boolean shouldSucceed) {
        if(shouldSucceed) {
            ratingService.rateCaretaker(
                    caretaker.getEmail(),
                    client.getAccountData().getEmail(),
                    paidCare.getId(),
                    rating,
                    "comment"
            );
            return;
        }

        assertThrows(DataIntegrityViolationException.class, () -> ratingService.rateCaretaker(
                caretaker.getEmail(),
                client.getAccountData().getEmail(),
                paidCare.getId(),
                rating,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_clientRatesHimself_shouldThrowIllegalActionException() {
        assertThrows(IllegalActionException.class, () -> ratingService.rateCaretaker(
                caretaker.getEmail(),
                clientSameAsCaretaker.getEmail(),
                paidCare.getId(),
                5,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_caretakerDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> ratingService.rateCaretaker(
                "invalidEmail",
                client.getAccountData().getEmail(),
                paidCare.getId(),
                5,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_careNotPaid_shouldThrowIllegalActionException() {
        assertThrows(IllegalActionException.class, () -> ratingService.rateCaretaker(
                caretaker.getEmail(),
                client.getAccountData().getEmail(),
                care.getId(),
                5,
                "comment"
        ));
    }

    @Test
    void deleteRating_shouldSucceed() {
        transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(caretaker, client, paidCare))
        );

        transactionTemplate.execute(status ->
                ratingService.deleteRating(caretaker.getEmail(), client.getAccountData().getEmail(), paidCare.getId())
        );

        transactionTemplate.execute(status -> {
            assertEquals(0, ratingRepository.count());
            return null;
        });
    }

    @Test
    void deleteRating_caretakerDoesNotExist_shouldThrow() {
        assertThrows(NotFoundException.class, () -> ratingService.deleteRating(
                "invalidEmail",
                client.getAccountData().getEmail(),
                paidCare.getId()
        ));
    }

    @Test
    void deleteRating_ratingDoesNotExist_shouldThrow() {
        assertThrows(NotFoundException.class, () -> ratingService.deleteRating(
                caretaker.getEmail(),
                client.getAccountData().getEmail(),
                paidCare.getId()
        ));
    }

    @Test
    void getRatings_shouldReturnRatings() {
        transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(caretaker, client, paidCare))
        );

        Page<RatingResponse> ratings = transactionTemplate.execute(status ->
                ratingService.getRatings(Pageable.ofSize(10), caretaker.getEmail())
        );

        assertEquals(1, ratings.getContent().size());
    }

    @Test
    void getRating_shouldReturnRating() {
        Rating rating = transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(caretaker, client, paidCare))
        );

        Rating foundRating = transactionTemplate.execute(status ->
                ratingService.getRating(caretaker.getEmail(), client.getEmail(), paidCare.getId())
        );

        assertEquals(rating.getCaretakerEmail(), foundRating.getCaretakerEmail());
        assertEquals(rating.getClientEmail(), foundRating.getClientEmail());
    }

    @Test
    void getRating_ratingDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> ratingService.getRating(
                caretaker.getEmail(),
                client.getEmail(),
                paidCare.getId()
        ));
    }

    private static Stream<Arguments> provideRatingParams() {
        return Stream.of(
                Arguments.of(-1, false),
                Arguments.of(0, false),
                Arguments.of(1, true),
                Arguments.of(2, true),
                Arguments.of(3, true),
                Arguments.of(4, true),
                Arguments.of(5, true),
                Arguments.of(6, false)
        );
    }


}

package com.example.petbuddybackend.service.rating;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.rating.Rating;
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
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.utils.exception.throweable.general.ForbiddenException;
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
import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCompletedCare;
import static com.example.petbuddybackend.testutils.mock.MockRatingProvider.createMockRating;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockClient;
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
    private Care care;
    private Care paidCare;


    @BeforeEach
    void init() {
        caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository);
        client = PersistenceUtils.addClient(appUserRepository, clientRepository);

        care = PersistenceUtils.addCare(
                careRepository, createMockCare(caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );
        paidCare = PersistenceUtils.addCare(
                careRepository, createMockCompletedCare(caretaker, client, animalRepository.findById("DOG").orElseThrow())
        );
    }

    @AfterEach
    void cleanUp() {
        ratingRepository.deleteAll();
        careRepository.deleteAll();
        appUserRepository.deleteAll();
    }


    @Test
    void testGetRating_shouldSortProperly() {
        List<String> fieldNames = List.of("rating", "comment");

        for(String fieldName : fieldNames) {
            assertDoesNotThrow(() -> ratingService.getRatings(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    caretaker.getEmail()
            ));
        }
    }

    @Test
    void rateCaretaker_shouldSucceed() {
        ratingService.rateCaretaker(
                client.getEmail(),
                paidCare.getId(),
                5,
                "comment"
        );

        transactionTemplate.execute(status -> {
            Rating rating = ratingRepository.getReferenceById(paidCare.getId());
            assertEquals(1, ratingRepository.count());
            assertEquals(5, rating.getRating());
            assertTrue(ValidationUtils.fieldsNotNullRecursive(rating, Set.of("client", "caretaker", "care")));
            return null;
        });
    }

    @Test
    void rateCaretaker_ratingExists_shouldUpdateRating() {
        transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(paidCare))
        );

        transactionTemplate.execute(status -> {
            ratingService.rateCaretaker(
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
            Rating rating = ratingRepository.getReferenceById(paidCare.getId());
            assertEquals(5, rating.getRating());
            assertEquals("new comment", rating.getComment());
            assertEquals(client.getEmail(), rating.getCare().getClient().getEmail());
            assertEquals(caretaker.getEmail(), rating.getCare().getCaretaker().getEmail());
            return null;
        });
    }

    @ParameterizedTest
    @MethodSource("provideRatingParams")
    void rateCaretaker_invalidRating_(int rating, boolean shouldSucceed) {
        if(shouldSucceed) {
            ratingService.rateCaretaker(
                    client.getAccountData().getEmail(),
                    paidCare.getId(),
                    rating,
                    "comment"
            );
            return;
        }

        assertThrows(DataIntegrityViolationException.class, () -> ratingService.rateCaretaker(
                client.getAccountData().getEmail(),
                paidCare.getId(),
                rating,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_careDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> ratingService.rateCaretaker(
                client.getEmail(),
                99L,
                5,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_careNotPaid_shouldThrowIllegalActionException() {
        assertThrows(IllegalActionException.class, () -> ratingService.rateCaretaker(
                client.getAccountData().getEmail(),
                care.getId(),
                5,
                "comment"
        ));
    }

    @Test
    void rateCaretaker_clientIsNotSubjectInCare_shouldThrowForbiddenException() {

        //given
        Care badCare = PersistenceUtils.addCare(
                careRepository,
                caretaker,
                PersistenceUtils.addClient(appUserRepository, clientRepository, createMockClient("anotherClient")),
                animalRepository.findById("DOG").get()
        );
        assertThrows(ForbiddenException.class, () -> ratingService.rateCaretaker(
                "notSubjectClient",
                badCare.getId(),
                2,
                "S"
        ));

    }

    @Test
    void deleteRating_shouldSucceed() {
        transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(paidCare))
        );

        transactionTemplate.execute(status ->
                ratingService.deleteRating(client.getAccountData().getEmail(), paidCare.getId())
        );

        transactionTemplate.execute(status -> {
            assertEquals(0, ratingRepository.count());
            return null;
        });
    }

    @Test
    void deleteRating_careDoesNotExist_shouldThrow() {
        assertThrows(NotFoundException.class, () -> ratingService.deleteRating(
                client.getAccountData().getEmail(),
                99L
        ));
    }

    @Test
    void deleteRating_ratingDoesNotExist_shouldThrow() {
        assertThrows(NotFoundException.class, () -> ratingService.deleteRating(
                client.getAccountData().getEmail(),
                paidCare.getId()
        ));
    }

    @Test
    void getRatings_shouldReturnRatings() {
        transactionTemplate.execute(status ->
                PersistenceUtils.addRatingToCaretaker(ratingRepository, createMockRating(paidCare))
        );

        Page<RatingResponse> ratings = transactionTemplate.execute(status ->
                ratingService.getRatings(Pageable.ofSize(10), caretaker.getEmail())
        );

        assertEquals(1, ratings.getContent().size());
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

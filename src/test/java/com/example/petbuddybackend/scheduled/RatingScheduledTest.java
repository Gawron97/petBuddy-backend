package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RatingScheduledTest {

    @MockBean
    private CaretakerRepository caretakerRepository;

    @MockBean
    private RatingRepository ratingRepository;

    @Autowired
    private RatingScheduled ratingScheduled;

    @Test
    void testUpdateRatingScoreOfCaretaker() {

        List<Caretaker> caretakers = getCaretakers();

        float expectedM = 4.5f;
        when(caretakerRepository.findAllByOrderByNumberOfRatingsDesc()).thenReturn(caretakers);
        when(ratingRepository.getAvgRating()).thenReturn(expectedM);

        ratingScheduled.updateRatingScoreOfCaretaker();

        int expectedC = 8;

        verify(caretakerRepository).updateRatingScore(expectedM, expectedC);

    }

    private List<Caretaker> getCaretakers() {

        Caretaker caretaker1 = createMockCaretaker("caretaker1");
        caretaker1.setNumberOfRatings(10);
        Caretaker caretaker2 = createMockCaretaker("caretaker2");
        caretaker2.setNumberOfRatings(8);
        Caretaker caretaker3 = createMockCaretaker("caretaker3");
        caretaker3.setNumberOfRatings(6);
        Caretaker caretaker4 = createMockCaretaker("caretaker4");
        caretaker4.setNumberOfRatings(4);

        return List.of(caretaker1, caretaker2, caretaker3, caretaker4);

    }

}

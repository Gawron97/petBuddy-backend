package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RatingScheduled {

    private final RatingRepository ratingRepository;
    private final CaretakerRepository caretakerRepository;

    @Scheduled(cron = "0 */30 * * * *")
    public void updateRatingScoreOfCaretaker() {

        Float m = ratingRepository.getAvgRating();
        List<Caretaker> caretakers = caretakerRepository.findAllByOrderByNumberOfRatingsDesc();
        Integer c = caretakers.get(caretakers.size() / 4).getNumberOfRatings();
        caretakerRepository.updateRatingScore(m, c);

    }

}

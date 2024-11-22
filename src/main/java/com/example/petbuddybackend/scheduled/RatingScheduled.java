package com.example.petbuddybackend.scheduled;

import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RatingScheduled {

    private final RatingRepository ratingRepository;
    private final CaretakerRepository caretakerRepository;

    @Scheduled(cron = "0 */30 * * * *")
    public void updateRatingScoreOfCaretaker() {

        Float averageOfAllCaretakersRating = ratingRepository.getAvgRating();
        Integer numberOfRatingsPercentile = ratingRepository.findPercentileOfNumberOfRatings(0.75f);
        caretakerRepository.updateRatingScore(averageOfAllCaretakersRating, numberOfRatingsPercentile);

    }

}

package com.example.petbuddybackend.service.rating;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.mapper.RatingMapper;
import com.example.petbuddybackend.service.user.UserService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final CareService careService;
    private final UserService userService;
    private final RatingMapper ratingMapper = RatingMapper.INSTANCE;

    public Page<RatingResponse> getRatings(Pageable pageable, String caretakerEmail) {
        return ratingRepository.findAllByCaretakerEmail(caretakerEmail, pageable)
                .map(ratingMapper::mapToRatingResponse);
    }

    public RatingResponse rateCaretaker(String clientEmail, Long careId, int rating,
                                        String comment) {

        Care care = careService.getCareById(careId);
        String caretakerEmail = care.getCaretaker().getEmail();
        userService.assertCaretakerAndClientExist(caretakerEmail, clientEmail);
        assertCareBetweenCaretakerAndClientIsValid(caretakerEmail, clientEmail, care);

        if(caretakerEmail.equals(clientEmail)) {
            throw new IllegalActionException("User cannot rate himself");
        }

        return ratingMapper.mapToRatingResponse(
                createOrUpdateRating(caretakerEmail, clientEmail, careId, rating, comment)
        );
    }

    public RatingResponse deleteRating(String clientEmail, Long careId) {
        Care care = careService.getCareById(careId);
        String caretakerEmail = care.getCaretaker().getEmail();
        userService.assertCaretakerAndClientExist(caretakerEmail, clientEmail);

        RatingKey ratingKey = new RatingKey(caretakerEmail, clientEmail, careId);
        Rating rating = getRating(ratingKey);
        ratingRepository.deleteById(ratingKey);

        return ratingMapper.mapToRatingResponse(rating);
    }

    private Rating createOrUpdateRating(String caretakerEmail, String clientEmail, Long careId, int rating,
                                        String comment) {
        Rating ratingEntity = getOrCreateRating(caretakerEmail, clientEmail, careId);

        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);

        return ratingRepository.save(ratingEntity);
    }

    private void assertCareBetweenCaretakerAndClientIsValid(String caretakerEmail, String clientEmail, Care care) {

        if(!care.getClient().getEmail().equals(clientEmail) || !care.getCaretaker().getEmail().equals(caretakerEmail)) {
            throw new IllegalActionException("This care does not exist between caretaker and client");
        }

        if(!care.getCaretakerStatus().equals(CareStatus.PAID) || !care.getClientStatus().equals(CareStatus.PAID)) {
            throw new IllegalActionException("Cannot rate unpaid care");
        }
    }

    private Rating getOrCreateRating(String caretakerEmail, String clientEmail, Long careId) {
        return ratingRepository.findById(new RatingKey(caretakerEmail, clientEmail, careId))
                .orElse(
                        Rating.builder()
                                .clientEmail(clientEmail)
                                .caretakerEmail(caretakerEmail)
                                .careId(careId)
                                .build()
                );
    }

    private Rating getRating(RatingKey ratingKey) {
        return ratingRepository.findById(ratingKey).orElseThrow(
                () -> new NotFoundException("Rating does not exist")
        );
    }

}

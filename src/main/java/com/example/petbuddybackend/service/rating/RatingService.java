package com.example.petbuddybackend.service.rating;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.service.care.CareService;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.mapper.RatingMapper;
import com.example.petbuddybackend.service.user.UserService;
import com.example.petbuddybackend.utils.exception.throweable.general.ForbiddenException;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private static final String RATING = "Rating";
    private static final String CANNOT_RATE_CARE_STATUS_EXCEPTION = "Cannot rate care of id %d. Required statuses: %s %s, actual statuses: %s %s";
    private static final String NOT_CLIENT_MESSAGE = "User %s is not a client of care %d";

    private final RatingRepository ratingRepository;
    private final CareService careService;
    private final UserService userService;
    private final CareStateMachine careStateMachine;
    private final RatingMapper ratingMapper = RatingMapper.INSTANCE;

    public Page<RatingResponse> getRatings(Pageable pageable, String caretakerEmail) {
        return ratingRepository.findAllByCare_Caretaker_Email(caretakerEmail, pageable)
                .map(this::refreshRatingPhotos)
                .map(ratingMapper::mapToRatingResponse);
    }

    @Transactional
    public RatingResponse rateCaretaker(String clientEmail, Long careId, int rating, String comment) {
        Care care = careService.getCareById(careId);
        assertCareOfClient(care, clientEmail);
        assertStatePermitsRating(care);

        Rating ratingEntity = createOrUpdateRating(clientEmail, careId, rating, comment);
        refreshRatingPhotos(ratingEntity);
        return ratingMapper.mapToRatingResponse(ratingEntity);
    }

    public RatingResponse deleteRating(String clientEmail, Long careId) {
        Rating rating = getRatingOfClient(careId, clientEmail);
        ratingRepository.delete(rating);
        refreshRatingPhotos(rating);
        return ratingMapper.mapToRatingResponse(rating);
    }

    private Rating refreshRatingPhotos(Rating rating) {
        userService.renewProfilePicture(rating.getCare().getClient().getAccountData());
        return rating;
    }

    private Rating createOrUpdateRating(String clientEmail, Long careId, int rating, String comment) {

        Care care = careService.getCareById(careId);
        assertCareOfClient(care, clientEmail);
        assertStatePermitsRating(care);

        Rating ratingEntity = ratingRepository.findById(careId).orElseGet(() ->
                Rating.builder()
                        .careId(care.getId())
                        .care(care)
                        .build()
        );

        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);

        return ratingRepository.save(ratingEntity);
    }

    private void assertCareOfClient(Care care, String clientEmail) {
        if(!care.getClient().getEmail().equals(clientEmail)) {
            throw new ForbiddenException(String.format(NOT_CLIENT_MESSAGE, clientEmail, care.getId()));
        }
    }

    private void assertStatePermitsRating(Care care) {
        CareStatus caretakerStatus = care.getCaretakerStatus();
        CareStatus clientStatus = care.getClientStatus();

        if(careStateMachine.canBeRated(care)) {
            throw new IllegalActionException(String.format(
                    CANNOT_RATE_CARE_STATUS_EXCEPTION,
                    care.getId(),
                    CareStatus.COMPLETED,
                    CareStatus.COMPLETED,
                    caretakerStatus,
                    clientStatus
            ));
        }
    }

    private Rating getRating(Long careId) {
        return ratingRepository.findById(careId).orElseThrow(
                () -> NotFoundException.withFormattedMessage(RATING, careId.toString())
        );
    }

    private Rating getRatingOfClient(Long careId, String clientEmail) {
        Rating rating = getRating(careId);
        Care care = careService.getCareById(careId);

        if(!care.getClient().getEmail().equals(clientEmail)) {
            throw new ForbiddenException("This rating is not yours");
        }

        return rating;
    }
}

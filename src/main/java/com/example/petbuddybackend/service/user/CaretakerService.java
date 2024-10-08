package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ModifyCaretakerDTO;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.service.mapper.RatingMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.specification.CaretakerSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final static String CARETAKER = "Caretaker";
    private final static String CLIENT = "Client";

    private final CaretakerRepository caretakerRepository;
    private final RatingRepository ratingRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;
    private final RatingMapper ratingMapper = RatingMapper.INSTANCE;

    private final ClientService clientService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<CaretakerDTO> getCaretakers(Pageable pageable,
                                            CaretakerSearchCriteria filters,
                                            Set<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters, offerFilters);

        return caretakerRepository.findAll(spec, pageable)
                .map(this::renewCaretakerProfilePicture)
                .map(caretakerMapper::mapToCaretakerDTO);
    }

    public CaretakerComplexInfoDTO getCaretaker(String caretakerEmail) {
        Caretaker caretaker = getCaretakerByEmail(caretakerEmail);
        renewCaretakerProfilePicture(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretaker);
    }

    public Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail));
    }

    public Page<RatingResponse> getRatings(Pageable pageable, String caretakerEmail) {
        return ratingRepository.findAllByCaretakerEmail(caretakerEmail, pageable)
                .map(ratingMapper::mapToRatingResponse);
    }

    public Rating getRating(String caretakerEmail, String clientEmail) {
        return getRating(new RatingKey(caretakerEmail, clientEmail));
    }

    public boolean caretakerExists(String caretakerEmail) {
        return caretakerRepository.existsById(caretakerEmail);
    }

    public RatingResponse rateCaretaker(String caretakerEmail, String clientEmail, int rating, String comment) {
        assertCaretakerAndClientExist(caretakerEmail, clientEmail);

        if(caretakerEmail.equals(clientEmail)) {
            throw new IllegalActionException("User cannot rate himself");
        }

        return ratingMapper.mapToRatingResponse(createOrUpdateRating(caretakerEmail, clientEmail, rating, comment));
    }

    public RatingResponse deleteRating(String caretakerEmail, String clientEmail) {
        assertCaretakerAndClientExist(caretakerEmail, clientEmail);

        RatingKey ratingKey = new RatingKey(caretakerEmail, clientEmail);
        Rating rating = getRating(ratingKey);
        ratingRepository.deleteById(ratingKey);

        return ratingMapper.mapToRatingResponse(rating);
    }

    public CaretakerComplexInfoDTO addCaretaker(ModifyCaretakerDTO caretakerDTO, String email) {
        assertCaretakerNotExists(email);
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretaker = caretakerMapper.mapToCaretaker(caretakerDTO, appUser);

        renewCaretakerProfilePicture(caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    public CaretakerComplexInfoDTO editCaretaker(ModifyCaretakerDTO caretakerDTO, String email) {
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretaker = getCaretakerByEmail(appUser.getEmail());

        renewCaretakerProfilePicture(caretaker);
        caretakerMapper.updateCaretakerFromDTO(caretakerDTO, caretaker);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretaker));
    }

    private Caretaker renewCaretakerProfilePicture(Caretaker caretaker) {
        userService.renewProfilePicture(caretaker.getAccountData());
        return caretaker;
    }

    private Rating createOrUpdateRating(String caretakerEmail, String clientEmail, int rating, String comment) {
        Rating ratingEntity = getOrCreateRating(caretakerEmail, clientEmail);

        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);

        return ratingRepository.save(ratingEntity);
    }

    private Rating getOrCreateRating(String caretakerEmail, String clientEmail) {
        return ratingRepository.findById(new RatingKey(caretakerEmail, clientEmail))
                .orElse(
                        Rating.builder()
                                .clientEmail(clientEmail)
                                .caretakerEmail(caretakerEmail)
                                .build()
                );
    }

    private void assertCaretakerAndClientExist(String caretakerEmail, String clientEmail) {
        if (!caretakerExists(caretakerEmail)) {
            throw NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail);
        }

        if (!clientService.clientExists(clientEmail)) {
            throw NotFoundException.withFormattedMessage(CLIENT, clientEmail);
        }
    }

    private Rating getRating(RatingKey ratingKey) {
        return ratingRepository.findById(ratingKey).orElseThrow(
                () -> new NotFoundException("Rating does not exist")
        );
    }

    private void assertCaretakerNotExists(String caretakerEmail) {
        if (caretakerExists(caretakerEmail)) {
            throw new IllegalActionException("Caretaker with email " + caretakerEmail + " already exists");
        }
    }
}

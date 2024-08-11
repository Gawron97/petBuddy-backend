package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.rating.RatingDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.repository.RatingRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.utils.exception.throweable.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import com.example.petbuddybackend.utils.specification.CaretakerSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final CaretakerRepository caretakerRepository;
    private final RatingRepository ratingRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;

    private final ClientService clientService;

    @Transactional
    public Page<CaretakerDTO> getCaretakers(Pageable pageable, CaretakerSearchCriteria filters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters);

        return caretakerRepository
                .findAll(spec, pageable)
                .map(caretakerMapper::mapToCaretakerDTO);
    }

    public Page<RatingDTO> getRatings(Pageable pageable, String caretakerEmail) {
        return ratingRepository.findAllByCaretakerEmail(caretakerEmail, pageable);
    }

    public boolean caretakerExists(String caretakerEmail) {
        return caretakerRepository.existsById(caretakerEmail);
    }

    public void rateCaretaker(String caretakerEmail, String clientEmail, int rating, String comment) {
        checkCaretakerAndClientExist(caretakerEmail, clientEmail);

        if(caretakerEmail.equals(clientEmail)) {
            throw new IllegalActionException("User cannot rate himself");
        }

        createOrUpdateRating(caretakerEmail, clientEmail, rating, comment);
    }

    public void deleteRating(String caretakerEmail, String clientEmail) {
        checkCaretakerAndClientExist(caretakerEmail, clientEmail);
        assertRatingExists(caretakerEmail, clientEmail);

        ratingRepository.deleteById(new RatingKey(caretakerEmail, clientEmail));
    }

    private void assertRatingExists(String caretakerEmail, String clientEmail) {
        if(!ratingRepository.existsById(new RatingKey(caretakerEmail, clientEmail))) {
            throw new NotFoundException("Rating does not exist");
        }
    }

    private void createOrUpdateRating(String caretakerEmail, String clientEmail, int rating, String comment) {
        Rating ratingEntity = ratingRepository.findById(new RatingKey(caretakerEmail, clientEmail))
                .orElse(
                        Rating.builder()
                                .clientEmail(clientEmail)
                                .caretakerEmail(caretakerEmail)
                                .build()
                );

        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);

        ratingRepository.save(ratingEntity);
    }

    private void checkCaretakerAndClientExist(String caretakerEmail, String clientEmail) {
        if (!caretakerExists(caretakerEmail)) {
            throw NotFoundException.withFormattedMessage("Caretaker", caretakerEmail);
        }

        if (!clientService.clientExists(clientEmail)) {
            throw NotFoundException.withFormattedMessage("Client", clientEmail);
        }
    }
}

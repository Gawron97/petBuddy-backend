package com.example.petbuddybackend.service.user;

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

    public void rateCaretaker(Long caretakerId, String clientUsername, int rating, String comment) {
        assertCaretakerExists(caretakerId);
        Long clientId = clientService.getClientIdByUsername(clientUsername);

        if(clientId.equals(caretakerId)) {
            throw new IllegalActionException("User cannot rate himself");
        }

        createOrUpdateRating(caretakerId, clientId, rating, comment);
    }

    public void deleteRating(Long caretakerId, String clientUsername) {
        assertCaretakerExists(caretakerId);
        Long clientId = clientService.getClientIdByUsername(clientUsername);
        assertRatingExists(caretakerId, clientId);

        ratingRepository.deleteById(new RatingKey(clientId, caretakerId));
    }

    private void assertRatingExists(Long caretakerId, Long clientId) {
        if(!ratingRepository.existsById(new RatingKey(clientId, caretakerId))) {
            throw new NotFoundException("Rating does not exist");
        }
    }

    private void createOrUpdateRating(Long caretakerId, Long clientId, int rating, String comment) {
        Rating ratingEntity = ratingRepository.findById(new RatingKey(clientId, caretakerId))
                .orElse(
                        Rating.builder()
                                .clientId(clientId)
                                .caretakerId(caretakerId)
                                .build()
                );

        ratingEntity.setRating(rating);
        ratingEntity.setComment(comment);

        ratingRepository.save(ratingEntity);
    }

    private void assertCaretakerExists(Long caretakerId) {
        if(!caretakerRepository.existsById(caretakerId)) {
            throw new NotFoundException("Caretaker with id " + caretakerId + " does not exist");
        }
    }
}

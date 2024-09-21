package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.availability.AvailabilityFilterDTO;
import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.offer.Offer;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaretakerService {

    private final CaretakerRepository caretakerRepository;
    private final RatingRepository ratingRepository;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;
    private final RatingMapper ratingMapper = RatingMapper.INSTANCE;

    private final ClientService clientService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<CaretakerDTO> getCaretakers(Pageable pageable, boolean hasSortByAvailabilityDaysMatch,
                                            Sort.Direction direction, CaretakerSearchCriteria filters,
                                            Set<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters, offerFilters);

        Page<Caretaker> caretakers = caretakerRepository.findAll(spec, pageable);

        List<CaretakerDTO> filteredCaretakers = caretakers
                .stream()
                .map(caretaker -> caretakerMapper.mapToCaretakerDTO(
                        caretaker,
                        calculateAvailabilityDaysMatch(caretaker, offerFilters)
                ))
                .collect(Collectors.toList());

        if(hasSortByAvailabilityDaysMatch) {
            sortCaretakers(
                    filteredCaretakers,
                    direction
            );
        }

        return new PageImpl<>(filteredCaretakers, pageable, caretakers.getTotalElements());

    }

    private void sortCaretakers(List<CaretakerDTO> caretakers, Sort.Direction direction) {
        caretakers.sort(getComparatorForSortByAvailabilityDaysMatch(direction));
    }

    private Comparator<CaretakerDTO> getComparatorForSortByAvailabilityDaysMatch(Sort.Direction direction) {
        return direction == Sort.Direction.ASC ?
                Comparator.comparingInt(CaretakerDTO::availabilityDaysMatch) :
                Comparator.comparingInt(CaretakerDTO::availabilityDaysMatch).reversed();
    }

    private Integer calculateAvailabilityDaysMatch(Caretaker caretaker, Set<OfferFilterDTO> offerFilters) {

        Integer totalAvailabilityDaysMatch = 0;

        for (OfferFilterDTO offerFilter : offerFilters) {
            if(!offerFilter.availabilities().isEmpty()) {
                totalAvailabilityDaysMatch += calculateAvailabilityDaysMatchForOffer(caretaker,
                        offerFilter.animalType(), offerFilter.availabilities());
            }
        }

        return totalAvailabilityDaysMatch;
    }

    private Integer calculateAvailabilityDaysMatchForOffer(Caretaker caretaker, String animalType, Set<AvailabilityFilterDTO> availabilityFilters) {

        Integer availabilityDaysMatchInOffer = 0;

        Offer matchingOffer = getOfferFromList(caretaker.getOffers(), animalType);

        for (AvailabilityFilterDTO availabilityFilter : availabilityFilters) {
            availabilityDaysMatchInOffer += calculateAvailabilityDaysMatchForAvailabilityFilter(availabilityFilter, matchingOffer);
        }

        return availabilityDaysMatchInOffer;
    }

    private Integer calculateAvailabilityDaysMatchForAvailabilityFilter(AvailabilityFilterDTO availabilityFilter, Offer matchingOffer) {

        if(matchingOffer.getAvailabilities().isEmpty()) {
            return 0;
        }

        return matchingOffer.getAvailabilities()
                .stream()
                .map(availability -> calculateOverlappingDays(availability, availabilityFilter))
                .reduce(0, Integer::sum);

    }

    private Integer calculateOverlappingDays(Availability availability, AvailabilityFilterDTO availabilityFilter) {

        ZonedDateTime overlapStart = availability.getAvailableFrom().isAfter(availabilityFilter.availableFrom())
                ? availability.getAvailableFrom()
                : availabilityFilter.availableFrom();

        ZonedDateTime overlapEnd = availability.getAvailableTo().isBefore(availabilityFilter.availableTo())
                ? availability.getAvailableTo()
                : availabilityFilter.availableTo();

        if(overlapStart.isAfter(overlapEnd)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }

    private Offer getOfferFromList(List<Offer> offers, String animalType) {
        return offers
                .stream()
                .filter(offer -> offer.getAnimal().getAnimalType().equals(animalType))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Offer for animal type " + animalType + " not found"));
    }

    public Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> new NotFoundException("Caretaker with email " + caretakerEmail + " not found"));
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
            throw NotFoundException.withFormattedMessage("Caretaker", caretakerEmail);
        }

        if (!clientService.clientExists(clientEmail)) {
            throw NotFoundException.withFormattedMessage("Client", clientEmail);
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

    public CaretakerComplexInfoDTO addCaretaker(CreateCaretakerDTO caretaker, String email) {
        assertCaretakerNotExists(email);
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretakerToSave = caretakerMapper.mapToCaretaker(caretaker);
        setAccountData(caretakerToSave, appUser);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretakerToSave));
    }

    private void setAccountData(Caretaker caretaker, AppUser appUser) {
        caretaker.setEmail(appUser.getEmail());
        caretaker.setAccountData(appUser);
    }

    public CaretakerComplexInfoDTO editCaretaker(UpdateCaretakerDTO caretaker, String email) {

        AppUser appUser = userService.getAppUser(email);
        Caretaker caretakerToSave = getCaretakerByEmail(appUser.getEmail());
        caretakerMapper.updateCaretakerFromDTO(caretaker, caretakerToSave);
        return caretakerMapper.mapToCaretakerComplexInfoDTO(caretakerRepository.save(caretakerToSave));

    }

}

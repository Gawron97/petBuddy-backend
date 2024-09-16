package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.criteriaSearch.CaretakerSearchCriteria;
import com.example.petbuddybackend.dto.offer.OfferConfigurationFilterDTO;
import com.example.petbuddybackend.dto.offer.OfferFilterDTO;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public Page<CaretakerDTO> getCaretakers(Pageable pageable, CaretakerSearchCriteria filters, List<OfferFilterDTO> offerFilters) {
        Specification<Caretaker> spec = CaretakerSpecificationUtils.toSpecification(filters);

        if(offerFilters.isEmpty()) {
            return caretakerRepository.findAll(spec, pageable)
                    .map(caretakerMapper::mapToCaretakerDTO);
        }

        List<Caretaker> prefilteredCaretakers = caretakerRepository.findAll(spec);
        List<Caretaker> filteredCaretakers = filterCaretakersByOfferFilter(prefilteredCaretakers, offerFilters);

        return new PageImpl<>(
                filteredCaretakers.stream().map(caretakerMapper::mapToCaretakerDTO).toList(),
                pageable,
                filteredCaretakers.size()
        );
        //TODO refactor
    }

    private List<Caretaker> filterCaretakersByOfferFilter(List<Caretaker> prefilteredCaretakers, List<OfferFilterDTO> offerFilters) {

        List<Caretaker> filteredCaretakers = new ArrayList<>();

        for(Caretaker caretaker: prefilteredCaretakers) {
            if(isCaretakerHasAllRequiredOffers(caretaker.getOffers(), offerFilters)) {
                filteredCaretakers.add(caretaker);
            }
        }

        return filteredCaretakers;

    }

    private boolean isCaretakerHasAllRequiredOffers(List<Offer> offers, List<OfferFilterDTO> offerFilters) {
        return offerFilters
                .stream()
                .allMatch(offerFilter -> isOfferMatching(offers, offerFilter));
    }

    private boolean isOfferMatching(List<Offer> offers, OfferFilterDTO offerFilter) {

        Offer matchingOffer = offers.stream()
                .filter(offer -> offer.getAnimal().getAnimalType().equals(offerFilter.animalType()))
                .findFirst()
                .orElse(null);
        if(matchingOffer == null) {
            return false;
        }

        if(offerFilter.offerConfigurations() == null || offerFilter.offerConfigurations().isEmpty()) {
            return true;
        }

        return isConfigurationsMatching(matchingOffer.getOfferConfigurations(), offerFilter.offerConfigurations());

    }

    private boolean isConfigurationsMatching(List<OfferConfiguration> matchingOfferConfigurations,
                                             List<OfferConfigurationFilterDTO> offerConfigurationsFilter) {


        for(OfferConfigurationFilterDTO offerConfigurationFilter: offerConfigurationsFilter) {
            boolean matchingOfferConfiguration = matchingOfferConfigurations
                    .stream()
                    .anyMatch(offerConfiguration -> isConfigurationMatching(offerConfiguration, offerConfigurationFilter));
            if(!matchingOfferConfiguration) {
                return false;
            }
        }
        return true;
    }

    private boolean isConfigurationMatching(OfferConfiguration offerConfiguration, OfferConfigurationFilterDTO offerConfigurationFilter) {

        boolean matchingConfigurationPrice = isPriceInConfigurationMatch(
                offerConfigurationFilter.minPrice(),
                offerConfigurationFilter.maxPrice(),
                offerConfiguration.getDailyPrice()
        );

        if(offerConfigurationFilter.attributes() == null || offerConfigurationFilter.attributes().isEmpty()) {
            return matchingConfigurationPrice;
        }

        Map<String, List<String>> attributesInOffer = getAttributesOfOffer(offerConfiguration.getOfferOptions());
        Map<String, List<String>> requiredAttributes = offerConfigurationFilter.attributes();

        return isAnimalAttributesInConfigurationMatch(attributesInOffer, requiredAttributes) &&
                matchingConfigurationPrice;

    }

    private Map<String, List<String>> getAttributesOfOffer(List<OfferOption> offerOptions) {
        return offerOptions.stream()
                .collect(Collectors.groupingBy(
                        offerOption -> offerOption.getAnimalAttribute().getAttributeName(),
                        Collectors.mapping(
                                offerOption -> offerOption.getAnimalAttribute().getAttributeValue(),
                                Collectors.toList()
                        )
                ));
    }

    private boolean isAnimalAttributesInConfigurationMatch(Map<String, List<String>> offerOptions, Map<String, List<String>> requiredAttributes) {



        return requiredAttributes
                .entrySet()
                .stream()
                .allMatch(requiredAttribute ->
                        !isRequiredAttributeGroupInOffer(requiredAttribute.getKey(), offerOptions) ||
                        isRequiredAttributeGroupValuesMatchOfferAttributeGroup(
                                requiredAttribute.getValue(),
                                offerOptions.get(requiredAttribute.getKey())
                        )
                );
    }

    private boolean isRequiredAttributeGroupInOffer(String requiredAttributeName, Map<String, List<String>> offerOptions) {
        return offerOptions.containsKey(requiredAttributeName);
    }

    private boolean isRequiredAttributeGroupValuesMatchOfferAttributeGroup(List<String> requiredAttributeValues,
                                                                           List<String> offerAttributeValues) {

        return requiredAttributeValues
                .stream()
                .allMatch(requiredAttributeValue -> offerAttributeValues
                        .stream()
                        .anyMatch(offerAttributeValue -> offerAttributeValue.equals(requiredAttributeValue))
                );
    }

    private boolean isPriceInConfigurationMatch(BigDecimal minPrice, BigDecimal maxPrice, BigDecimal dailyPrice) {
        return dailyPrice.compareTo(minPrice) >= 0 && dailyPrice.compareTo(maxPrice) <= 0;
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

    public CaretakerDTO addCaretaker(CreateCaretakerDTO caretaker, String email) {
        assertCaretakerNotExists(email);
        AppUser appUser = userService.getAppUser(email);
        Caretaker caretakerToSave = caretakerMapper.mapToCaretaker(caretaker);
        setAccountData(caretakerToSave, appUser);
        return caretakerMapper.mapToCaretakerDTO(caretakerRepository.save(caretakerToSave));
    }

    private void setAccountData(Caretaker caretaker, AppUser appUser) {
        caretaker.setEmail(appUser.getEmail());
        caretaker.setAccountData(appUser);
    }

    public CaretakerDTO editCaretaker(UpdateCaretakerDTO caretaker, String email) {

        AppUser appUser = userService.getAppUser(email);
        Caretaker caretakerToSave = getCaretakerByEmail(appUser.getEmail());
        caretakerMapper.updateCaretakerFromDTO(caretaker, caretakerToSave);
        return caretakerMapper.mapToCaretakerDTO(caretakerRepository.save(caretakerToSave));

    }

}

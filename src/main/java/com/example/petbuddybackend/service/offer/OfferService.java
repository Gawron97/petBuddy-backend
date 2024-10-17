package com.example.petbuddybackend.service.offer;

import com.example.petbuddybackend.dto.availability.AvailabilityRangeDTO;
import com.example.petbuddybackend.dto.availability.CreateOffersAvailabilityDTO;
import com.example.petbuddybackend.dto.offer.ModifyConfigurationDTO;
import com.example.petbuddybackend.dto.offer.ModifyOfferDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.service.animal.AnimalService;
import com.example.petbuddybackend.service.mapper.OfferConfigurationMapper;
import com.example.petbuddybackend.service.mapper.OfferMapper;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.general.UnauthorizedException;
import com.example.petbuddybackend.utils.exception.throweable.offer.AvailabilityDatesOverlappingException;
import com.example.petbuddybackend.utils.exception.throweable.offer.OfferConfigurationDuplicatedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final CaretakerService caretakerService;
    private final OfferRepository offerRepository;
    private final OfferConfigurationRepository offerConfigurationRepository;
    private final AnimalService animalService;
    private final OfferMapper offerMapper = OfferMapper.INSTANCE;
    private final OfferConfigurationMapper offerConfigurationMapper = OfferConfigurationMapper.INSTANCE;

    @Transactional
    public OfferDTO addOrEditOffer(ModifyOfferDTO offer, String caretakerEmail) {
        Caretaker caretaker = caretakerService.getCaretakerByEmail(caretakerEmail);

        Offer modifiyngOffer = getOrCreateOffer(caretakerEmail, offer.animal().animalType(),
                caretaker, offer.description());

        if(StringUtils.hasText(offer.description())) {
            modifiyngOffer.setDescription(offer.description());
        }

        setOfferConfigurations(offer, modifiyngOffer);
        setOfferAnimalAmenities(offer, modifiyngOffer);

        return offerMapper.mapToOfferDTO(offerRepository.save(modifiyngOffer));

    }

    @Transactional
    public OfferDTO addConfigurationsForOffer(Long offerId, List<ModifyConfigurationDTO> configurations, String caretakerEmail) {

        Offer offer = getOffer(offerId);
        assertOfferIsModifyingByOwnerCaretaker(offer, caretakerEmail);

        List<OfferConfiguration> offerConfigurations = createAdditionalConfigurationsForOffer(configurations, offer);

        offer.getOfferConfigurations().addAll(offerConfigurations);
        return offerMapper.mapToOfferDTO(offerRepository.save(offer));

    }

    @Transactional
    public OfferDTO setAmenitiesForOffer(Long offerId, Set<String> amenities, String caretakerEmail) {

        Offer offer = getOffer(offerId);
        assertOfferIsModifyingByOwnerCaretaker(offer, caretakerEmail);

        Set<AnimalAmenity> newAnimalAmenities = createAdditionalAnimalAmenitiesForOffer(amenities, offer);

        Set<AnimalAmenity> animalAmenitiesInOffer = offer.getAnimalAmenities();
        removeNotProvidedAnimalAmenities(animalAmenitiesInOffer, amenities);
        animalAmenitiesInOffer.addAll(newAnimalAmenities);
        return offerMapper.mapToOfferDTO(offerRepository.save(offer));

    }

    @Transactional
    public OfferDTO deleteConfiguration(Long configurationId, String userEmail) {

        OfferConfiguration offerConfiguration = getOfferConfiguration(configurationId);
        assertOfferIsModifyingByOwnerCaretaker(offerConfiguration.getOffer(), userEmail);
        Offer offer = offerConfiguration.getOffer();
        offer.getOfferConfigurations().remove(offerConfiguration);
        return offerMapper.mapToOfferDTO(offerRepository.save(offer));

    }

    @Transactional
    public OfferConfigurationDTO editConfiguration(Long configurationId, ModifyConfigurationDTO configuration, String userEmail) {

        OfferConfiguration offerConfiguration = getOfferConfiguration(configurationId);
        assertOfferIsModifyingByOwnerCaretaker(offerConfiguration.getOffer(), userEmail);

        List<OfferConfiguration> restOfferConfigurations = offerConfiguration.getOffer().getOfferConfigurations().stream()
                .filter(restOfferConfiguration -> !restOfferConfiguration.getId().equals(offerConfiguration.getId()))
                .toList();
        assertOffersAreModifyingByOwnerCaretaker(restOfferConfigurations.stream().map(OfferConfiguration::getOffer).toList(), userEmail);

        offerConfiguration.setDescription(configuration.description());
        offerConfiguration.setDailyPrice(configuration.dailyPrice());
        editConfigurationSelectedOptions(offerConfiguration, configuration);
        checkForDuplicateConfiguration(restOfferConfigurations, offerConfiguration);
        return offerConfigurationMapper.mapToOfferConfigurationDTO(offerConfigurationRepository.save(offerConfiguration));
    }

    @Transactional
    public OfferDTO deleteAmenitiesFromOffer(List<String> amenities, String userEmail, Long offerId) {
        Offer offer = getOffer(offerId);
        assertOfferIsModifyingByOwnerCaretaker(offer, userEmail);

        Set<AnimalAmenity> animalAmenities = offer.getAnimalAmenities();
        amenities.forEach(
                amenity -> animalAmenities.removeIf(
                        animalAmenity -> animalAmenity.getAmenity().getName().equals(amenity)));
        return offerMapper.mapToOfferDTO(offerRepository.save(offer));

    }

    @Transactional
    public List<OfferDTO> setAvailabilityForOffers(CreateOffersAvailabilityDTO createOffersAvailability,
                                                   String caretakerEmail) {

        assertAvailabilityRangesNotOverlapping(createOffersAvailability.availabilityRanges());
        List<Offer> modifiedOffers = createOffersAvailability.offerIds()
                .stream()
                .map(offerId -> setAvailabilityForOffer(offerId, createOffersAvailability.availabilityRanges(), caretakerEmail))
                .toList();

        return offerRepository.saveAll(modifiedOffers)
                .stream()
                .map(offerMapper::mapToOfferDTO)
                .toList();
    }

    private void removeNotProvidedAnimalAmenities(Set<AnimalAmenity> animalAmenitiesInOffer, Set<String> amenities) {
        animalAmenitiesInOffer.removeIf(animalAmenity -> !amenities.contains(animalAmenity.getAmenity().getName()));
    }

    private Offer getOrCreateOffer(String caretakerEmail, String animalType, Caretaker caretaker,
                                   String description) {
        return offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerEmail, animalType)
                .orElse(Offer.builder()
                        .caretaker(caretaker)
                        .animal(animalService.getAnimal(animalType))
                        .description(description)
                        .build());

    }

    private void setOfferConfigurations(ModifyOfferDTO offer, Offer modifiyngOffer) {

        if(CollectionUtil.isNotEmpty(offer.offerConfigurations())) {
            List<OfferConfiguration> offerConfigurations = createAdditionalConfigurationsForOffer(offer.offerConfigurations(), modifiyngOffer);

            modifiyngOffer.getOfferConfigurations().addAll(offerConfigurations);
        }

    }

    private List<OfferConfiguration> createAdditionalConfigurationsForOffer(List<ModifyConfigurationDTO> offerConfigurations,
                                                                            Offer offer) {
        List<OfferConfiguration> newOfferConfigurations = new ArrayList<>();
        for(ModifyConfigurationDTO offerConfiguration : offerConfigurations) {
            OfferConfiguration configuration = createConfiguration(offerConfiguration, offer);
            checkForDuplicateConfiguration(
                    Stream.concat(
                        offer.getOfferConfigurations().stream(),
                        newOfferConfigurations.stream()
                    ).toList(),
                    configuration
            );

            newOfferConfigurations.add(configuration);
        }
        return newOfferConfigurations;

    }

    private void checkForDuplicateConfiguration(List<OfferConfiguration> oldConfigurations, OfferConfiguration configuration) {

        List<List<AnimalAttribute>> animalAttributesLists =
                oldConfigurations.stream()
                        .map(OfferConfiguration::getOfferOptions)
                        .map(offerOptions -> offerOptions.stream()
                                .map(OfferOption::getAnimalAttribute)
                                .sorted(Comparator.comparingLong(AnimalAttribute::getId))
                                .collect(Collectors.toList()))
                        .toList();

        List<AnimalAttribute> newAnimalAttributes = configuration.getOfferOptions().stream()
                .map(OfferOption::getAnimalAttribute)
                .sorted(Comparator.comparingLong(AnimalAttribute::getId))
                .toList();

        if(animalAttributesLists.stream().anyMatch(animalAttributes -> animalAttributes.equals(newAnimalAttributes))) {
            throw new OfferConfigurationDuplicatedException(MessageFormat.format(
                    "Offer configuration with animal attributes {0} already exists",
                    newAnimalAttributes.stream()
                            .map(AnimalAttribute::toString)
                            .collect(Collectors.joining(", "))
            ));
        }

    }

    private OfferConfiguration createConfiguration(ModifyConfigurationDTO offerConfiguration, Offer offer) {
        OfferConfiguration newOfferConfiguration = OfferConfiguration.builder()
                .dailyPrice(offerConfiguration.dailyPrice())
                .description(offerConfiguration.description())
                .offer(offer)
                .build();

        List<OfferOption> offerOptions =
                createOfferOptionsForConfiguration(offerConfiguration.selectedOptions(), newOfferConfiguration);

        newOfferConfiguration.setOfferOptions(offerOptions);
        return newOfferConfiguration;
    }

    private List<OfferOption> createOfferOptionsForConfiguration(Map<String, List<String>> selectedOptions,
                                                                 OfferConfiguration offerConfiguration) {

        List<OfferOption> offerOptions = new ArrayList<>();
        for(Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
            String attributeName = entry.getKey();
            for(String attributeValue : entry.getValue()) {
                AnimalAttribute animalAttribute = animalService.getAnimalAttribute(offerConfiguration.getOffer().getAnimal().getAnimalType(),
                        attributeName, attributeValue);
                offerOptions.add(createOfferOption(animalAttribute, offerConfiguration));
            }
        }
        return offerOptions;

    }

    private OfferOption createOfferOption(AnimalAttribute animalAttribute, OfferConfiguration offerConfiguration) {
        return OfferOption.builder()
                .animalAttribute(animalAttribute)
                .offerConfiguration(offerConfiguration)
                .build();
    }

    private void setOfferAnimalAmenities(ModifyOfferDTO offer, Offer modifiyngOffer) {

        if(CollectionUtil.isNotEmpty(offer.animalAmenities())) {
            Set<AnimalAmenity> animalAmenities = createAdditionalAnimalAmenitiesForOffer(offer.animalAmenities(), modifiyngOffer);

            modifiyngOffer.getAnimalAmenities().addAll(animalAmenities);
        }

    }

    private Set<AnimalAmenity> createAdditionalAnimalAmenitiesForOffer(Set<String> animalAmenities, Offer modifiyngOffer) {

        Set<AnimalAmenity> newAnimalAmenities = new HashSet<>();
        for(String animalAmenity : animalAmenities) {
            AnimalAmenity newAnimalAmenity = animalService.getAnimalAmenity(animalAmenity, modifiyngOffer.getAnimal().getAnimalType());
            if(!checkDuplicateForAnimalAmenity(
                    Stream.concat(
                            modifiyngOffer.getAnimalAmenities().stream(),
                            newAnimalAmenities.stream()
                    ).toList(), newAnimalAmenity
            )) {
                newAnimalAmenities.add(newAnimalAmenity);
            }
        }

        return new HashSet<>(newAnimalAmenities);

    }

    private boolean checkDuplicateForAnimalAmenity(List<AnimalAmenity> oldAnimalAmenities, AnimalAmenity animalAmenity) {
        return oldAnimalAmenities.stream().anyMatch(oldAnimalAmenity -> oldAnimalAmenity.equals(animalAmenity));
    }

    private OfferConfiguration getOfferConfiguration(Long id) {
        return offerConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer configuration with id " + id + " not found"));
    }

    private void editConfigurationSelectedOptions(OfferConfiguration editingConfiguration, ModifyConfigurationDTO configuration) {
        List<OfferOption> offerOptions = editingConfiguration.getOfferOptions();

        offerOptions.removeIf(offerOption ->
                !configuration.selectedOptions().containsKey(offerOption.getAnimalAttribute().getAttributeName()) ||
                !configuration.selectedOptions().get(offerOption.getAnimalAttribute().getAttributeName())
                        .contains(offerOption.getAnimalAttribute().getAttributeValue()));

        for(Map.Entry<String, List<String>> entry : configuration.selectedOptions().entrySet()) {
            String attributeName = entry.getKey();
            for(String attributeValue : entry.getValue()) {
                AnimalAttribute animalAttribute = animalService.getAnimalAttribute(editingConfiguration.getOffer().getAnimal().getAnimalType(),
                        attributeName, attributeValue);
                if(offerOptions.stream().noneMatch(offerOption -> offerOption.getAnimalAttribute().equals(animalAttribute))) {
                    offerOptions.add(createOfferOption(animalAttribute, editingConfiguration));
                }
            }
        }
    }

    private Offer setAvailabilityForOffer(Long offerId, List<AvailabilityRangeDTO> availabilityRanges, String caretakerEmail) {

        Offer offerToModify = getOffer(offerId);
        assertOfferIsModifyingByOwnerCaretaker(offerToModify, caretakerEmail);
        Set<Availability> availabilities = createAvailabilities(availabilityRanges, offerToModify);

        replaceAvailabilitiesInOffer(offerToModify, availabilities);

        return offerToModify;
    }

    private Offer getOffer(Long offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer with id " + offerId + " not found"));
    }

    private void replaceAvailabilitiesInOffer(Offer offerToModify, Set<Availability> availabilities) {
        Set<Availability> availabilitiesFromOffer = offerToModify.getAvailabilities();
        availabilitiesFromOffer.clear();
        availabilitiesFromOffer.addAll(availabilities);
    }

    private void assertOffersAreModifyingByOwnerCaretaker(List<Offer> offers, String caretakerEmail) {
        offers.forEach(offer -> assertOfferIsModifyingByOwnerCaretaker(offer, caretakerEmail));
    }

    private void assertOfferIsModifyingByOwnerCaretaker(Offer offer, String caretakerEmail) {
        if(!offer.getCaretaker().getEmail().equals(caretakerEmail)) {
            throw new UnauthorizedException("Caretaker with email: " + caretakerEmail +
                    "is trying to modify offer that does not belong to him");
        }
    }

    private Set<Availability> createAvailabilities(List<AvailabilityRangeDTO> availabilityRanges, Offer offer) {
        return availabilityRanges.stream()
                .map(availabilityRange -> createAvailability(availabilityRange, offer))
                .collect(Collectors.toSet());
    }

    private Availability createAvailability(AvailabilityRangeDTO availabilityRange, Offer offer) {

        return Availability.builder()
                .availableFrom(availabilityRange.availableFrom())
                .availableTo(availabilityRange.availableTo())
                .offer(offer)
                .build();

    }

    private void assertAvailabilityRangesNotOverlapping(List<AvailabilityRangeDTO> availabilityRanges) {
        List<AvailabilityRangeDTO> sortedRanges = new ArrayList<>(availabilityRanges);
        sortedRanges.sort(Comparator.comparing(AvailabilityRangeDTO::availableFrom));

        for(int i = 1; i < availabilityRanges.size(); i++) {
            AvailabilityRangeDTO previous = availabilityRanges.get(i - 1);
            AvailabilityRangeDTO current = availabilityRanges.get(i);

            if (previous.overlaps(current)) {
                throw new AvailabilityDatesOverlappingException(
                        MessageFormat.format(
                                "Availability range {0} overlaps with availability range {1}",
                                previous.toString(),
                                current.toString()
                        )
                );
            }
        }
    }

}

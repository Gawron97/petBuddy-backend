package com.example.petbuddybackend.service.offer;

import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.service.mapper.OfferConfigurationMapper;
import com.example.petbuddybackend.service.mapper.OfferMapper;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.exception.throweable.AnimalAmenityDuplicatedInOfferException;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.OfferConfigurationDuplicatedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.stereotype.Service;
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
    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;
    private final AnimalAmenityRepository animalAmenityRepository;
    private final OfferMapper offerMapper = OfferMapper.INSTANCE;
    private final OfferConfigurationMapper offerConfigurationMapper = OfferConfigurationMapper.INSTANCE;

    public OfferDTO addOrEditOffer(OfferDTO offer, String caretakerEmail) {
        Caretaker caretaker = caretakerService.getCaretaker(caretakerEmail);

        Offer modifiyngOffer = getOrCreateCaretakerOffer(caretakerEmail, offer.animal().animalType(),
                caretaker, offer.description());

        if(StringUtils.hasText(offer.description())) {
            modifiyngOffer.setDescription(offer.description());
        }

        setOfferConfigurations(offer, modifiyngOffer);
        setOfferAnimalAmenities(offer, modifiyngOffer);

        return offerMapper.mapToOfferDTO(offerRepository.save(modifiyngOffer));

    }

    private void setOfferConfigurations(OfferDTO offer, Offer modifiyngOffer) {

        if(CollectionUtil.isNotEmpty(offer.offerConfigurations())) {
            List<OfferConfiguration> offerConfigurations = createConfigurationsForOffer(offer.offerConfigurations(), modifiyngOffer);
            if(CollectionUtil.isNotEmpty(modifiyngOffer.getOfferConfigurations())) {
                modifiyngOffer.getOfferConfigurations().addAll(offerConfigurations);
            } else {
                modifiyngOffer.setOfferConfigurations(offerConfigurations);
            }
        }

    }

    private void setOfferAnimalAmenities(OfferDTO offer, Offer modifiyngOffer) {

        if(CollectionUtil.isNotEmpty(offer.animalAmenities())) {
            Set<AnimalAmenity> animalAmenities = createAnimalAmenitiesForOffer(offer.animalAmenities(), modifiyngOffer);
            if(CollectionUtil.isNotEmpty(modifiyngOffer.getAnimalAmenities())) {
                modifiyngOffer.getAnimalAmenities().addAll(animalAmenities);
            } else {
                modifiyngOffer.setAnimalAmenities(animalAmenities);
            }

        }

    }

    private Set<AnimalAmenity> createAnimalAmenitiesForOffer(List<String> animalAmenities, Offer modifiyngOffer) {

        List<AnimalAmenity> newAnimalAmenities = new ArrayList<>();
        for(String animalAmenity : animalAmenities) {
            AnimalAmenity newAnimalAmenity = getAnimalAmenity(animalAmenity, modifiyngOffer.getAnimal().getAnimalType());
            checkDuplicateForAnimalAmenity(
                    Stream.concat(
                            Optional.ofNullable(modifiyngOffer.getAnimalAmenities()).orElse(Collections.emptySet()).stream(),
                            newAnimalAmenities.stream()
                    ).toList(), newAnimalAmenity
            );
            newAnimalAmenities.add(newAnimalAmenity);
        }

        return new HashSet<>(newAnimalAmenities);

    }

    private void checkDuplicateForAnimalAmenity(List<AnimalAmenity> oldAnimalAmenities, AnimalAmenity animalAmenity) {
        if(oldAnimalAmenities.stream().anyMatch(oldAnimalAmenity -> oldAnimalAmenity.equals(animalAmenity))) {
            throw new AnimalAmenityDuplicatedInOfferException(MessageFormat.format(
                    "Animal amenity with name {0} already exists",
                    animalAmenity.getAmenity().getName()
            ));
        }
    }

    private AnimalAmenity getAnimalAmenity(String amenityName, String animalType) {
        return animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType(amenityName, animalType)
                .orElseThrow(() -> new NotFoundException("Animal amenity with name " + amenityName + " and animal type "
                        + animalType + " not found"));
    }

    private List<OfferConfiguration> createConfigurationsForOffer(List<OfferConfigurationDTO> offerConfigurations,
                                                                  Offer offer) {
        List<OfferConfiguration> newOfferConfigurations = new ArrayList<>();
        for(OfferConfigurationDTO offerConfiguration : offerConfigurations) {
            OfferConfiguration configuration = createConfiguration(offerConfiguration, offer);
            checkForDuplicateConfiguration(Stream.concat(
                    Optional.ofNullable(offer.getOfferConfigurations()).orElse(Collections.emptyList()).stream(),
                    newOfferConfigurations.stream()
            ).toList(), configuration);

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

    private OfferConfiguration createConfiguration(OfferConfigurationDTO offerConfiguration, Offer offer) {
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
                AnimalAttribute animalAttribute = getAnimalAttribute(offerConfiguration.getOffer().getAnimal().getAnimalType(),
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

    private AnimalAttribute getAnimalAttribute(String animalType, String attributeName, String attributeValue) {
        return animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(animalType, attributeName, attributeValue)
                .orElseThrow(() -> new NotFoundException("Animal attribute with name " + attributeName + " and value " + attributeValue + " not found"));
    }

    private Offer getOrCreateCaretakerOffer(String caretakerEmail, String animalType, Caretaker caretaker,
                                            String description) {
        return offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerEmail, animalType)
                .orElse(Offer.builder()
                        .caretaker(caretaker)
                        .animal(getAnimal(animalType))
                        .description(description)
                        .build());

    }

    private OfferConfiguration getOfferConfiguration(Long id) {
        return offerConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer configuration with id " + id + " not found"));
    }

    private Animal getAnimal(String animalType) {
        return animalRepository.findById(animalType)
                .orElseThrow(() -> new NotFoundException("Animal with type " + animalType + " not found"));
    }

    public OfferDTO deleteConfiguration(Long configurationId) {

        OfferConfiguration offerConfiguration = getOfferConfiguration(configurationId);
        Offer offer = offerConfiguration.getOffer();
        offer.getOfferConfigurations().remove(offerConfiguration);
        return offerMapper.mapToOfferDTO(offerRepository.save(offer));

    }

    public OfferConfigurationDTO editConfiguration(Long configurationId, OfferConfigurationDTO configuration) {

        OfferConfiguration offerConfiguration = getOfferConfiguration(configurationId);

        List<OfferConfiguration> restOfferConfigurations = offerConfiguration.getOffer().getOfferConfigurations().stream()
                .filter(restOfferConfiguration -> !restOfferConfiguration.getId().equals(offerConfiguration.getId()))
                .toList();

        offerConfiguration.setDescription(configuration.description());
        offerConfiguration.setDailyPrice(configuration.dailyPrice());
        editConfigurationSelectedOptions(offerConfiguration, configuration);
        checkForDuplicateConfiguration(restOfferConfigurations, offerConfiguration);
        return offerConfigurationMapper.mapToOfferConfigurationDTO(offerConfigurationRepository.save(offerConfiguration));
    }

    private void editConfigurationSelectedOptions(OfferConfiguration editingConfiguration, OfferConfigurationDTO configuration) {
        List<OfferOption> offerOptions = editingConfiguration.getOfferOptions();

        offerOptions.removeIf(offerOption -> !configuration.selectedOptions().containsKey(offerOption.getAnimalAttribute().getAttributeName()) ||
                !configuration.selectedOptions().get(offerOption.getAnimalAttribute().getAttributeName()).contains(offerOption.getAnimalAttribute().getAttributeValue()));

        for(Map.Entry<String, List<String>> entry : configuration.selectedOptions().entrySet()) {
            String attributeName = entry.getKey();
            for(String attributeValue : entry.getValue()) {
                AnimalAttribute animalAttribute = getAnimalAttribute(editingConfiguration.getOffer().getAnimal().getAnimalType(),
                        attributeName, attributeValue);
                if(offerOptions.stream().noneMatch(offerOption -> offerOption.getAnimalAttribute().equals(animalAttribute))) {
                    offerOptions.add(createOfferOption(animalAttribute, editingConfiguration));
                }
            }
        }
    }

}

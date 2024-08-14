package com.example.petbuddybackend.service.offer;

import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferOptionRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.service.mapper.OfferMapper;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.OfferConfigurationAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final CaretakerRepository caretakerRepository;
    private final OfferRepository offerRepository;
    private final OfferConfigurationRepository offerConfigurationRepository;
    private final OfferOptionRepository offerOptionRepository;
    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;
    private final OfferMapper offerMapper = OfferMapper.INSTANCE;

    public OfferDTO addOrEditOffer(OfferDTO offer, String caretakerEmail) {
        Caretaker caretaker = getCaretaker(caretakerEmail);

        Offer modifiyngOffer = getOrCreateCaretakerOffer(caretakerEmail, offer.animal().animalType(),
                caretaker, offer.description());

        List<OfferConfiguration> offerConfigurations = createConfigurationsForOffer(offer.offerConfigurations(), modifiyngOffer);
        modifiyngOffer.setOfferConfigurations(offerConfigurations);
        return offerMapper.mapToOfferDTO(persistOfferWithConfigurations(modifiyngOffer));

    }

    private Offer persistOfferWithConfigurations(Offer offer) {
        Offer savedOffer = offerRepository.save(offer);
        offerConfigurationRepository.saveAll(offer.getOfferConfigurations());
        offerOptionRepository.saveAll(offer.getOfferConfigurations().stream()
                .map(OfferConfiguration::getOfferOptions)
                .flatMap(List::stream)
                .toList());
        return savedOffer;
    }

    private List<OfferConfiguration> createConfigurationsForOffer(List<OfferConfigurationDTO> offerConfigurations,
                                                                  Offer offer) {
        List<OfferConfiguration> newOfferConfigurations = new ArrayList<>();
        for(OfferConfigurationDTO offerConfiguration : offerConfigurations) {
            OfferConfiguration configuration = createConfiguration(offerConfiguration, offer);
            checkForDuplicateConfiguration(offer.getOfferConfigurations(), configuration);
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

        animalAttributesLists.stream()
                .filter(animalAttributes -> animalAttributes.equals(newAnimalAttributes))
                .findAny()
                .ifPresent(animalAttributes -> {
                    throw new OfferConfigurationAlreadyExistsException(MessageFormat.format(
                            "Offer configuration with animal attributes {0} already exists",
                            newAnimalAttributes.stream()
                                    .map(AnimalAttribute::toString)
                                    .collect(Collectors.joining(", "))
                    ));
                });

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

    private Caretaker getCaretaker(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> new NotFoundException("Caretaker with email " + caretakerEmail + " not found"));
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

    private Animal getAnimal(String animalType) {
        return animalRepository.findById(animalType)
                .orElseThrow(() -> new NotFoundException("Animal with type " + animalType + " not found"));
    }

}

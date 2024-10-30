package com.example.petbuddybackend.service.animal;

import com.example.petbuddybackend.dto.animal.AnimalComplexInfoDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.service.mapper.AnimalMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private static final String ANIMAL_ATTRIBUTE_NOT_FOUND_MESSAGE =
            "Animal attribute with name {0}, value {1} and animal type {2} not found";

    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;
    private final AnimalAmenityRepository animalAmenityRepository;

    private final AnimalMapper animalMapper = AnimalMapper.INSTANCE;

    private static final String ANIMAL = "Animal";

    public AnimalAmenity getAnimalAmenity(String amenityName, String animalType) {
        return animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType(amenityName, animalType.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Animal amenity with name " + amenityName + " and animal type "
                        + animalType + " not found"));
    }

    public AnimalAttribute getAnimalAttribute(String animalType, String attributeName, String attributeValue) {
        return animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(
                animalType.toUpperCase(),
                attributeName,
                attributeValue
                )
                .orElseThrow(() -> new NotFoundException(MessageFormat.format(
                        ANIMAL_ATTRIBUTE_NOT_FOUND_MESSAGE,
                        attributeName, attributeValue, animalType.toUpperCase())
                ));
    }

    public Animal getAnimal(String animalType) {
        return animalRepository.findById(animalType.toUpperCase())
                .orElseThrow(() -> NotFoundException.withFormattedMessage(ANIMAL, animalType));
    }

    public Set<AnimalAttribute> getAnimalAttributesOfAnimal(List<Long> animalAttributeIds) {
        if(animalAttributeIds == null || animalAttributeIds.isEmpty()) {
            return Collections.emptySet();
        }

        return animalAttributeRepository.findDistinctByIdIn(animalAttributeIds);
    }

    public Set<AnimalAttribute> getAnimalAttributes(String animalType, Map<String, List<String>> attributes) {
        return attributes.entrySet()
                .stream()
                .flatMap(entry ->
                        entry.getValue()
                                .stream()
                                .map(value -> getAnimalAttribute(animalType, entry.getKey(), value))
                )
                .collect(Collectors.toSet());
    }

    public Map<String, List<String>> getAnimalAttributesOfAnimal(String animalType) {
        return animalAttributeRepository.findAllByAnimal_AnimalType(animalType.toUpperCase())
                .stream()
                .collect(Collectors.groupingBy(
                        AnimalAttribute::getAttributeName,
                        Collectors.mapping(AnimalAttribute::getAttributeValue, Collectors.toList())
                ));
    }

    public Set<String> getAnimals() {
        return animalRepository.findAll()
                .stream()
                .map(Animal::getAnimalType)
                .collect(Collectors.toSet());
    }

    public Set<String> getAmenitiesForAnimal(String animalType) {
        return animalAmenityRepository.findAllByAnimal_AnimalType(animalType.toUpperCase())
                .stream()
                .map(animalAmenity -> animalAmenity.getAmenity().getName())
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<AnimalComplexInfoDTO> getAnimalsWithAttributesAndAmenities() {
        return animalRepository
                .findAll()
                .stream()
                .map(animalMapper::mapToAnimalComplexInfoDTO)
                .toList();
    }
}

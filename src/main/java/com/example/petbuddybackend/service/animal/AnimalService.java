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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimalService {

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
                .orElseThrow(() -> new NotFoundException("Animal attribute with name " +
                        attributeName + " and value " + attributeValue + " not found"));
    }

    public Animal getAnimal(String animalType) {
        return animalRepository.findById(animalType.toUpperCase())
                .orElseThrow(() -> NotFoundException.withFormattedMessage(ANIMAL, animalType));
    }

    public Set<AnimalAttribute> getAnimalAttributesOfAnimal(List<Long> animalAttributeIds) {
        return new HashSet<>(animalAttributeRepository.findAllById(animalAttributeIds));
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

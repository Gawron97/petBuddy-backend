package com.example.petbuddybackend.service.animal;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;
    private final AnimalAmenityRepository animalAmenityRepository;

    public AnimalAmenity getAnimalAmenity(String amenityName, String animalType) {
        return animalAmenityRepository.findByAmenity_NameAndAnimal_AnimalType(amenityName, animalType)
                .orElseThrow(() -> new NotFoundException("Animal amenity with name " + amenityName + " and animal type "
                        + animalType + " not found"));
    }

    public AnimalAttribute getAnimalAttribute(String animalType, String attributeName, String attributeValue) {
        return animalAttributeRepository.findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(animalType, attributeName, attributeValue)
                .orElseThrow(() -> new NotFoundException("Animal attribute with name " + attributeName + " and value " + attributeValue + " not found"));
    }

    public Animal getAnimal(String animalType) {
        return animalRepository.findById(animalType)
                .orElseThrow(() -> new NotFoundException("Animal with type " + animalType + " not found"));
    }

    public Set<AnimalAttribute> getAnimalAttributes(List<Long> animalAttributeIds) {
        return new HashSet<>(animalAttributeRepository.findAllById(animalAttributeIds));
    }
}

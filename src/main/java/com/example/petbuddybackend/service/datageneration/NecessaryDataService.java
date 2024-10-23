package com.example.petbuddybackend.service.datageneration;

import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.utils.provider.resources.AmenityProvider;
import com.example.petbuddybackend.utils.provider.resources.AnimalAmenityProvider;
import com.example.petbuddybackend.utils.provider.resources.AnimalAttributeProvider;
import com.example.petbuddybackend.utils.provider.resources.AnimalProvider;
import com.example.petbuddybackend.utils.resourcestructure.AmenityConfig;
import com.example.petbuddybackend.utils.resourcestructure.AnimalAttributeConfig;
import com.example.petbuddybackend.utils.resourcestructure.AnimalConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;

@Service
@Profile("dev | test")
@RequiredArgsConstructor
public class NecessaryDataService {

    private final AnimalProvider animalProvider;
    private final AnimalAttributeProvider animalAttributeProvider;
    private final AmenityProvider amenityProvider;
    private final AnimalAmenityProvider animalAmenityProvider;

    public List<Animal> createAnimals() {
        return animalProvider.getAnimalConfigs().stream()
                .map(this::createAnimal)
                .toList();
    }

    private Animal createAnimal(AnimalConfig animalConfig) {
        return Animal.builder()
                .animalType(animalConfig.animalType())
                .build();
    }

    private Animal createAnimal(String animalType) {
        return Animal.builder()
                .animalType(animalType)
                .build();
    }

    private Animal getAnimal(String animalType, List<Animal> animals) {
        return animals.stream()
                .filter(animal -> animal.getAnimalType().equals(animalType))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(MessageFormat.format("Animal {0} not found", animalType)));
    }

    public List<AnimalAttribute> createAnimalAttributes(List<Animal> animals) {
        return animalAttributeProvider.getAnimalAttributeConfigs().stream()
                .map(animalAttributeConfig -> {
                    try {
                        return createAnimalAttribute(animalAttributeConfig,
                                getAnimal(animalAttributeConfig.animalType(), animals));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                MessageFormat.format(
                                        "Animal {0} not found during generating animal attribute {1}",
                                        animalAttributeConfig.animalType(), animalAttributeConfig
                                ));
                    }
                })
                .toList();
    }

    private AnimalAttribute createAnimalAttribute(AnimalAttributeConfig animalAttributeConfig, Animal animal) {
        return AnimalAttribute.builder()
                .animal(animal)
                .attributeName(animalAttributeConfig.attributeName())
                .attributeValue(animalAttributeConfig.attributeValue())
                .build();
    }

    public List<Amenity> createAmenities() {
        return amenityProvider.getAmenityConfigs().stream()
                .map(this::createAmenity)
                .toList();
    }

    private Amenity createAmenity(AmenityConfig amenityConfig) {
        return Amenity.builder()
                .name(amenityConfig.amenity())
                .build();
    }

    private Amenity getAmenity(String amenity, List<Amenity> amenities) {
        return amenities.stream()
                .filter(possibleAmenity -> possibleAmenity.getName().equals(amenity))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(MessageFormat.format("Amenity {0} not found", amenity)));
    }

    public List<AnimalAmenity> createAnimalAmenities(List<Amenity> amenities, List<Animal> animals) {

        return animalAmenityProvider.getAnimalAmenityConfigs()
                .stream().map(animalAmenityConfig -> {
                    try {
                        return createAnimalAmenity(
                                getAnimal(animalAmenityConfig.animalType(), animals),
                                getAmenity(animalAmenityConfig.amenity(), amenities)
                        );
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                MessageFormat.format(
                                        "Animal {0} or Amenity {1} not found during generating animal amenity {2}",
                                        animalAmenityConfig.animalType(),
                                        animalAmenityConfig.amenity(),
                                        animalAmenityConfig
                                ));
                    }
                })
                .toList();

    }

    private AnimalAmenity createAnimalAmenity(Animal animal, Amenity amenity) {
        return AnimalAmenity.builder()
                .animal(animal)
                .amenity(amenity)
                .build();
    }

}

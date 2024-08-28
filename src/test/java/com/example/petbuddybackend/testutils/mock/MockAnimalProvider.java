package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.entity.amenity.Amenity;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockAnimalProvider {

    public static Animal createMockAnimal(String animalType) {
        return Animal.builder()
                .animalType(animalType)
                .build();
    }

    public static AnimalAttribute createAnimalAttribute(String attributeName, String attributeValue, Animal animal) {
        return AnimalAttribute.builder()
                .animal(animal)
                .attributeName(attributeName)
                .attributeValue(attributeValue)
                .build();
    }


    public static AnimalAmenity createAnimalAmenity(Animal animal, String amenity) {
        return AnimalAmenity.builder()
                .animal(animal)
                .amenity(Amenity.builder().name(amenity).build())
                .build();
    }
}

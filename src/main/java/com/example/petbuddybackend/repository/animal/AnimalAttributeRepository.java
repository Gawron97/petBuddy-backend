package com.example.petbuddybackend.repository.animal;

import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimalAttributeRepository extends JpaRepository<AnimalAttribute, Long> {

    Optional<AnimalAttribute> findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(String animal_animalType,
                                                                                       String attributeName,
                                                                                       String attributeValue);

}

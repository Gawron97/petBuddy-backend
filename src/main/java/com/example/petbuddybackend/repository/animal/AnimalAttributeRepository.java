package com.example.petbuddybackend.repository.animal;

import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AnimalAttributeRepository extends JpaRepository<AnimalAttribute, Long> {

    Optional<AnimalAttribute> findByAnimal_AnimalTypeAndAttributeNameAndAttributeValue(String animal_animalType,
                                                                                       String attributeName,
                                                                                       String attributeValue);

    Set<AnimalAttribute> findDistinctByIdIn(List<Long> animalAttributeIds);

}

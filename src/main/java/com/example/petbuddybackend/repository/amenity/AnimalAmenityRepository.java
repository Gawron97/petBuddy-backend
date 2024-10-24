package com.example.petbuddybackend.repository.amenity;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimalAmenityRepository extends JpaRepository<AnimalAmenity, Long> {

    Optional<AnimalAmenity> findByAmenity_NameAndAnimal_AnimalType(String name, String animalType);

    List<AnimalAmenity> findAllByAnimal_AnimalType(String animalType);

}

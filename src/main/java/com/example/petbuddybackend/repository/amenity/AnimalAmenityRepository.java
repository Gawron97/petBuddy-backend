package com.example.petbuddybackend.repository.amenity;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimalAmenityRepository extends JpaRepository<AnimalAmenity, Long> {

    Optional<AnimalAmenity> findByAmenity_AmenityAndAnimal_AnimalType(String amenity, String animalType);

}

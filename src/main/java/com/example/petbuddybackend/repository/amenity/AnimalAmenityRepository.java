package com.example.petbuddybackend.repository.amenity;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalAmenityRepository extends JpaRepository<AnimalAmenity, Long> {
}

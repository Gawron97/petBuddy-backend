package com.example.petbuddybackend.repository.amenity;

import com.example.petbuddybackend.entity.amenity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, String> {
}

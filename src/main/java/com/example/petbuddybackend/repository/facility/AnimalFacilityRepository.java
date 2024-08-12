package com.example.petbuddybackend.repository.facility;

import com.example.petbuddybackend.entity.facility.AnimalFacility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalFacilityRepository extends JpaRepository<AnimalFacility, Long> {
}

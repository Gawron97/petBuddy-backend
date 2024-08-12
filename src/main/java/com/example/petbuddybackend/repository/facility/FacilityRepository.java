package com.example.petbuddybackend.repository.facility;

import com.example.petbuddybackend.entity.facility.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, String> {
}

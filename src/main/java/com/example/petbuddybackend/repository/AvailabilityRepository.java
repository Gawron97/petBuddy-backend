package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.availability.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}

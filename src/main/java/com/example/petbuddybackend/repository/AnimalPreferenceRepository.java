package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.animal.AnimalPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalPreferenceRepository extends JpaRepository<AnimalPreference, Long> {
}

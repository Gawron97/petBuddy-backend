package com.example.petbuddybackend.repository.animal;

import com.example.petbuddybackend.entity.animal.AnimalAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalAttributeValueRepository extends JpaRepository<AnimalAttributeValue, Long> {
}

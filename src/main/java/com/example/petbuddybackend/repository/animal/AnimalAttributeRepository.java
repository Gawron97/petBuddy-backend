package com.example.petbuddybackend.repository.animal;

import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalAttributeRepository extends JpaRepository<AnimalAttribute, Long> {
}

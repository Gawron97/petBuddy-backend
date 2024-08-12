package com.example.petbuddybackend.repository.animal;

import com.example.petbuddybackend.entity.animal.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, String> {
}

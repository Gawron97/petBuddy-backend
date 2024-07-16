package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.user.Caretaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaretakerRepository extends JpaRepository<Caretaker, String> {
}

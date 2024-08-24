package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.care.Care;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareRepository extends JpaRepository<Care, Long> {
}

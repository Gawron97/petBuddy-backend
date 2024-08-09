package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.animal.CaretakerOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalTakenCareOfRepository extends JpaRepository<CaretakerOffer, Long> {
}

package com.example.petbuddybackend.repository.rating;

import com.example.petbuddybackend.entity.rating.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findAllByCare_Caretaker_Email(String caretakerEmail, Pageable pageable);
}

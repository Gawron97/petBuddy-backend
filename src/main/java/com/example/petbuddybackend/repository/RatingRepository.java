package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, RatingKey> {

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.caretakerId = ?1")
    Optional<Float> getAvgRatingByCaretakerId(Long caretakerId);
}

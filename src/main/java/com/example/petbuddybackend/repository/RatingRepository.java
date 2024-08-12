package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RatingRepository extends JpaRepository<Rating, RatingKey> {

    Page<Rating> findAllByCaretakerEmail(String caretakerEmail, Pageable pageable);
}

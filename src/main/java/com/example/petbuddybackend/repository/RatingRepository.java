package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.dto.rating.RatingRequest;
import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.rating.RatingKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface RatingRepository extends JpaRepository<Rating, RatingKey> {

    @Query("""
            SELECT new com.example.petbuddybackend.dto.rating.RatingResponse(
                r.clientEmail,
                r.caretakerEmail,
                r.rating,
                r.comment
            )
            FROM Rating r
            WHERE r.caretakerEmail = :caretakerEmail
    """)
    Page<RatingResponse> findAllByCaretakerEmail(String caretakerEmail, Pageable pageable);
}

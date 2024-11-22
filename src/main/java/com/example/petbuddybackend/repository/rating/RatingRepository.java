package com.example.petbuddybackend.repository.rating;

import com.example.petbuddybackend.entity.rating.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findAllByCare_Caretaker_Email(String caretakerEmail, Pageable pageable);

    @Query("""
        SELECT AVG(r.rating)
        FROM Rating r
        """)
    Float getAvgRating();

    /**
     * Find the percentile of the number of ratings of caretakers that have at least one rating.
     */
    @Query(value = """
        
        WITH number_of_ratings_query AS ( 
            SELECT COUNT(*) AS number_of_ratings
            FROM Rating r
            JOIN Care c ON r.care_id = c.id
            GROUP BY c.caretaker_email
        )
        SELECT PERCENTILE_CONT(:percentile)
        WITHIN GROUP (ORDER BY number_of_ratings)
        FROM number_of_ratings_query
        """, nativeQuery = true)
    Integer findPercentileOfNumberOfRatings(Float percentile);


}

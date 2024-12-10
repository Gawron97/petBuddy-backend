package com.example.petbuddybackend.repository.user;

import com.example.petbuddybackend.entity.user.Caretaker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface CaretakerRepository extends JpaRepository<Caretaker, String>, JpaSpecificationExecutor<Caretaker> {

    Page<Caretaker> findAll(Specification<Caretaker> spec, Pageable pageable);

    @Transactional
    @Modifying
    @Query("""
        UPDATE Caretaker c
        SET c.ratingScore = (((c.avgRating * c.numberOfRatings) + (:confidence * :avgGlobalRating)) /
                 (c.numberOfRatings + :confidence))
        """)
    void updateRatingScore(Float avgGlobalRating, Integer confidence);

    @Query("""
        
        SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
        FROM Client c
        JOIN c.followingCaretakers as following
        WHERE c.email = :clientEmail AND following.email = :caretakerEmail
        
        """)
    boolean isCaretakerFollowedByClient(String caretakerEmail, String clientEmail);

}

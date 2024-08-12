package com.example.petbuddybackend.repository.offer;

import com.example.petbuddybackend.entity.offer.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}

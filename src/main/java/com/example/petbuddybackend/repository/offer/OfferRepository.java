package com.example.petbuddybackend.repository.offer;

import com.example.petbuddybackend.entity.offer.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findByCaretaker_EmailAndAnimal_AnimalType(String caretakerEmail, String animalType);

}

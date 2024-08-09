package com.example.petbuddybackend.repository;

import com.example.petbuddybackend.entity.animal.AnimalPreference;
import com.example.petbuddybackend.entity.offer.CaretakerOffer;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaretakerOfferRepository extends JpaRepository<CaretakerOffer, Long> {

    Optional<CaretakerOffer> findByCaretaker_EmailAndAnimalPreference_Id(String caretaker_email, Long animalPreference_id);

}

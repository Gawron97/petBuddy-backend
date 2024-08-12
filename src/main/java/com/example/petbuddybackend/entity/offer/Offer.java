package com.example.petbuddybackend.entity.offer;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.facility.AnimalFacility;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretakerEmail", "animalType" }) }
)
public class Offer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caretakerEmail", nullable = false, updatable = false)
    private Caretaker caretaker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY)
    private List<OfferConfiguration> offerConfigurations;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "offer_animal_facility",
            joinColumns = @JoinColumn(name = "offerId"),
            inverseJoinColumns = @JoinColumn(name = "animalFacilityId")
    )
    private List<AnimalFacility> animalFacilities;

}

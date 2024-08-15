package com.example.petbuddybackend.entity.offer;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretakerEmail", "animalType" }) }
)
@Getter @Setter
@EqualsAndHashCode(of = {"caretaker", "animal"})
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

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<OfferConfiguration> offerConfigurations;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "offer_animal_amenity",
            joinColumns = @JoinColumn(name = "offerId"),
            inverseJoinColumns = @JoinColumn(name = "animalAmenityId")
    )
    private List<AnimalAmenity> animalAmenities;

}

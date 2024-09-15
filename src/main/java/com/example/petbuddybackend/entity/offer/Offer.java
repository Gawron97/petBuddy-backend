package com.example.petbuddybackend.entity.offer;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretakerEmail", "animalType" }) }
)
@Getter @Setter
@EqualsAndHashCode(of = {"caretaker", "animal"})
public class Offer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1500)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caretakerEmail", nullable = false, updatable = false)
    private Caretaker caretaker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<OfferConfiguration> offerConfigurations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "offer_animal_amenity",
            joinColumns = @JoinColumn(name = "offerId"),
            inverseJoinColumns = @JoinColumn(name = "animalAmenityId")
    )
    @Builder.Default
    private Set<AnimalAmenity> animalAmenities = new HashSet<>();

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private Set<Availability> availabilities = new HashSet<>();

    @PrePersist
    public void prePersist() {
        assertAnimalAttributesMatchWithAnimalType();
    }

    @PreUpdate
    public void preUpdate() {
        assertAnimalAttributesMatchWithAnimalType();
    }

    private void assertAnimalAttributesMatchWithAnimalType() {

        if(offerConfigurations != null) {
            offerConfigurations.stream()
                    .filter(offerConfiguration -> offerConfiguration.getOfferOptions() != null &&
                            !offerConfiguration.getOfferOptions().isEmpty())
                    .flatMap(offerConfiguration -> offerConfiguration.getOfferOptions().stream())
                    .filter(offerOption ->
                            !offerOption.getAnimalAttribute().getAnimal().getAnimalType().equals(animal.getAnimalType()))
                    .findAny()
                    .ifPresent(offerOption -> {
                        throw new IllegalArgumentException("Animal attribute does not match with animal type");
                    });
        }

    }

}

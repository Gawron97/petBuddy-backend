package com.example.petbuddybackend.entity.animal;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.offer.Offer;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(of = "animalType")
@ToString(of = "animalType")
public class Animal {

    @Id
    private String animalType;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private List<AnimalAttribute> animalAttributes;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private List<AnimalAmenity> animalFacilities;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private List<Offer> offers;

}

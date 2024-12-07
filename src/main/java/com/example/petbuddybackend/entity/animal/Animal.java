package com.example.petbuddybackend.entity.animal;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.offer.Offer;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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
    @Column(length = 15)
    private String animalType;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<AnimalAttribute> animalAttributes = new ArrayList<>();

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<AnimalAmenity> animalAmenities = new ArrayList<>();

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

}

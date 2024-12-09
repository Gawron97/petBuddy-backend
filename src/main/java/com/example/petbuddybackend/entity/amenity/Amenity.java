package com.example.petbuddybackend.entity.amenity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Amenity {

    @Id
    @Column(length = 25)
    private String name;

    @OneToMany(mappedBy = "amenity", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<AnimalAmenity> animalAmenities;

}

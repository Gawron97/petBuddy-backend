package com.example.petbuddybackend.entity.amenity;

import com.example.petbuddybackend.entity.animal.Animal;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class AnimalAmenity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "amenity", nullable = false, updatable = false)
    private Amenity amenity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

}

package com.example.petbuddybackend.entity.amenity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Amenity {

    @Id
    private String amenity;

    @OneToMany(mappedBy = "amenity", fetch = FetchType.LAZY)
    private List<AnimalAmenity> animalAmenities;

}

package com.example.petbuddybackend.entity.animal;

import com.example.petbuddybackend.entity.facility.AnimalFacility;
import com.example.petbuddybackend.entity.offer.Offer;
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
public class Animal {

    @Id
    private String animalType;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private List<AnimalAttribute> animalAttributes;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private List<AnimalFacility> animalFacilities;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private List<Offer> offers;

}

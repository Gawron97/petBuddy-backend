package com.example.petbuddybackend.entity.animal;

import com.example.petbuddybackend.entity.animal.keys.AnimalAttributeKey;
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
@IdClass(AnimalAttributeKey.class)
public class AnimalAttribute {

    @Id
    private String animalType;

    @Id
    private String attributeName;

    @MapsId
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "animalType", referencedColumnName = "animalType", updatable = false)
    private Animal animal;

    @OneToMany(mappedBy = "animalAttribute", fetch = FetchType.LAZY)
    private List<AnimalAttributeValue> animalAttributeValues;

}

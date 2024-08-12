package com.example.petbuddybackend.entity.animal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"animalType", "attributeName", "attributeValue"})
        }
)
public class AnimalAttributeValue {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //add unnatural id to easier pass to other tables as foreign key

    private String attributeValue;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumns({
            @JoinColumn(name = "animalType", referencedColumnName = "animalType"),
            @JoinColumn(name = "attributeName", referencedColumnName = "attributeName")
    })
    private AnimalAttribute animalAttribute;

}

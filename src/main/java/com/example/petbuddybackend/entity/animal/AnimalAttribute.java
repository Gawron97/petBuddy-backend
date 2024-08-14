package com.example.petbuddybackend.entity.animal;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"animalType", "attributeName", "attributeValue"})
        }
)
@EqualsAndHashCode(of = "id")
@ToString(of = {"animal", "attributeName", "attributeValue"})
public class AnimalAttribute {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //add unnatural id to easier pass to other tables as foreign key

    @Nonnull
    private String attributeName;

    @Nonnull
    private String attributeValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

}

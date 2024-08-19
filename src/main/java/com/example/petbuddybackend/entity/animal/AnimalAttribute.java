package com.example.petbuddybackend.entity.animal;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @Column(length = 30, nullable = false)
    private String attributeName;

    @Column(length = 60, nullable = false)
    private String attributeValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

}

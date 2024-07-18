package com.example.petbuddybackend.entity.animal;

import com.example.petbuddybackend.entity.user.Caretaker;
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
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretaker_email", "animalType" }) }
)
public class Animal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caretaker_email", nullable = false)
    private Caretaker caretaker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalType animalType;
}

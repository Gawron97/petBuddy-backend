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
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretaker_email", "animal_option_id" }) }
)
public class CaretakerOffer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "caretaker_email", nullable = false)
    private Caretaker caretaker;

    @ManyToOne
    @JoinColumn(name = "animal_option_id", referencedColumnName = "id", updatable = false)
    private AnimalOption animalOption;

}

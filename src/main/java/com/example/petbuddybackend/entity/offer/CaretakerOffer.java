package com.example.petbuddybackend.entity.offer;

import com.example.petbuddybackend.entity.animal.AnimalPreference;
import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.annotation.Nonnull;
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
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretaker_email", "animal_preference_id" }) }
)
public class CaretakerOffer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nonnull
    private Double dailyPrice;

    @ManyToOne
    @JoinColumn(name = "caretaker_email", nullable = false, updatable = false)
    private Caretaker caretaker;

    @ManyToOne
    @JoinColumn(name = "animal_preference_id", referencedColumnName = "id", updatable = false)
    private AnimalPreference animalPreference;

}

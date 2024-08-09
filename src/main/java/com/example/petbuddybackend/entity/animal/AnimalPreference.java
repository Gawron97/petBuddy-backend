package com.example.petbuddybackend.entity.animal;

import com.example.petbuddybackend.entity.offer.CaretakerOffer;
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
@Table(
        uniqueConstraints = { @UniqueConstraint(columnNames = { "animalType", "optionName" }) }
)
public class AnimalPreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalType animalType;

    @Column(nullable = false)
    private String animalDetails;

    @OneToMany(mappedBy = "animalOption", fetch = FetchType.LAZY)
    private List<CaretakerOffer> caretakerOffers;

}

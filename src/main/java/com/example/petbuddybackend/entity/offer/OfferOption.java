package com.example.petbuddybackend.entity.offer;

import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"animalAttributeId", "offerConfigurationId"})
    }
)
@ToString(of = {"id", "animalAttribute"})
public class OfferOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalAttributeId", nullable = false, updatable = false)
    private AnimalAttribute animalAttribute;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offerConfigurationId", nullable = false, updatable = false)
    private OfferConfiguration offerConfiguration;

}

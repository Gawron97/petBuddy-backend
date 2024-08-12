package com.example.petbuddybackend.entity.offer;

import com.example.petbuddybackend.entity.animal.AnimalAttributeValue;
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
        @UniqueConstraint(columnNames = {"animalAttributeValueId", "offerConfigurationId"})
    }
)
public class OfferOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "animalAttributeValueId", nullable = false, updatable = false)
    private AnimalAttributeValue animalAttributeValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offerConfigurationId", nullable = false, updatable = false)
    private OfferConfiguration offerConfiguration;

}

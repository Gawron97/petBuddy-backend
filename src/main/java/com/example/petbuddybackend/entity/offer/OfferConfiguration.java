package com.example.petbuddybackend.entity.offer;

import jakarta.annotation.Nonnull;
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
public class OfferConfiguration {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nonnull
    private Double dailyPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer", nullable = false, updatable = false)
    private Offer offer;

    @OneToMany(mappedBy = "offerConfiguration", fetch = FetchType.EAGER)
    private List<OfferOption> offerOptions;

}

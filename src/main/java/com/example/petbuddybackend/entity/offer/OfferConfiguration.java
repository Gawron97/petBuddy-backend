package com.example.petbuddybackend.entity.offer;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString(of = {"id", "description", "dailyPrice", "offerOptions"})
public class OfferConfiguration {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Nonnull
    private Double dailyPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false, updatable = false)
    private Offer offer;

    @OneToMany(mappedBy = "offerConfiguration", fetch = FetchType.EAGER)
    private List<OfferOption> offerOptions;

}

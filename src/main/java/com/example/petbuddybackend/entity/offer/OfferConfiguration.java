package com.example.petbuddybackend.entity.offer;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Column(length = 1500)
    private String description;

    @Column(nullable = false)
    private BigDecimal dailyPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false, updatable = false)
    private Offer offer;

    @OneToMany(mappedBy = "offerConfiguration", fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<OfferOption> offerOptions = new ArrayList<>();

}

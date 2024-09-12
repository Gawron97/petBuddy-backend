package com.example.petbuddybackend.entity.availability;

import com.example.petbuddybackend.entity.offer.Offer;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter @Setter
public class Availability {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ZonedDateTime availableFrom;
    private ZonedDateTime availableTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerId", nullable = false, updatable = false)
    private Offer offer;

}

package com.example.petbuddybackend.entity.availability;

import com.example.petbuddybackend.entity.offer.Offer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter @Setter
@Table(
        uniqueConstraints = { @UniqueConstraint(columnNames = { "offerId", "availableFrom", "availableTo" }) }
)
@EqualsAndHashCode(of = {"availableFrom", "availableTo", "offer"})
@Check(constraints = "available_from <= available_to")
public class Availability {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate availableFrom;

    @Column(nullable = false)
    private LocalDate availableTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerId", nullable = false, updatable = false)
    private Offer offer;

}

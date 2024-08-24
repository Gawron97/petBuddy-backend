package com.example.petbuddybackend.entity.care;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter @Setter
@Table(
        uniqueConstraints = { @UniqueConstraint(columnNames = { "caretakerEmail", "clientEmail", "from", "to" }) }
)
@Check(constraints = "client_email <> caretaker_email")
public class Care {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ZonedDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    private CareStatus caretakerStatus;

    @Enumerated(EnumType.STRING)
    private CareStatus clientStatus;

    private LocalDate from;

    private LocalDate to;

    private String description;

    private Double dailyPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "care_animal_attribute",
            joinColumns = @JoinColumn(name = "careId"),
            inverseJoinColumns = @JoinColumn(name = "animalAttributeId")
    )
    private Set<AnimalAttribute> animalAttributes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caretakerEmail", nullable = false, updatable = false)
    private Caretaker caretaker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientEmail", nullable = false, updatable = false)
    private Client client;

}

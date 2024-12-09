package com.example.petbuddybackend.entity.care;

import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter @Setter
@Check(constraints = "client_email <> caretaker_email")
@Check(constraints = "daily_price >= 0")
public class Care {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareStatus caretakerStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareStatus clientStatus;

    @Column(nullable = false)
    private LocalDate careStart;

    @Column(nullable = false)
    private LocalDate careEnd;

    @Column(length = 1500)
    private String description;

    @Column(nullable = false)
    private BigDecimal dailyPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animalType", nullable = false, updatable = false)
    private Animal animal;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "care_animal_attribute",
            joinColumns = @JoinColumn(name = "careId"),
            inverseJoinColumns = @JoinColumn(name = "animalAttributeId")
    )
    @Builder.Default
    private Set<AnimalAttribute> animalAttributes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caretakerEmail", nullable = false, updatable = false)
    private Caretaker caretaker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientEmail", nullable = false, updatable = false)
    private Client client;

    @OneToMany(mappedBy = "care", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<CareStatusesHistory> careStatusesHistory = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if(submittedAt == null) {
            submittedAt = ZonedDateTime.now();
        }
        assertAnimalAttributesMatchWithAnimalType();
    }

    @PreUpdate
    public void preUpdate() {
        assertAnimalAttributesMatchWithAnimalType();
    }

    private void assertAnimalAttributesMatchWithAnimalType() {
        if(animalAttributes.stream()
                .anyMatch(animalAttribute ->
                        !animalAttribute.getAnimal().getAnimalType().equals(animal.getAnimalType()))) {

            throw new IllegalArgumentException("Animal attributes must match with animal type");
        }
    }

}

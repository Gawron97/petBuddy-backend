package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.animal.AnimalType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Caretaker {

    @Id
    private String email;

    @Column(nullable = false, length = 14)
    private String phoneNumber;

    @Column(length = 1500)
    private String description;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = AnimalType.class, fetch = FetchType.EAGER)
    @JoinTable(name = "animals_taken_care_of", joinColumns = @JoinColumn(name = "email"))
    @Column(name = "takes_care_of", nullable = false)
    Set<AnimalType> animalsTakenCareOf;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Address address;

    @OneToOne(cascade = { CascadeType.MERGE, CascadeType.REMOVE }, optional = false)
    @PrimaryKeyJoinColumn
    private AppUser accountData;
}

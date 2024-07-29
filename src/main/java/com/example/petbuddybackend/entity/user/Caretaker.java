package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.animal.AnimalTakenCareOf;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "caretaker")
    private List<AnimalTakenCareOf> animalsTakenCareOf;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Address address;

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;
}

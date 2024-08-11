package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.animal.AnimalTakenCareOf;
import com.example.petbuddybackend.entity.rating.Rating;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

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

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;

    @Basic(fetch = FetchType.LAZY)
    @Formula("(SELECT AVG(r.rating) FROM Rating r WHERE r.caretaker_email = email)")
    private Float avgRating;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "caretaker", fetch = FetchType.LAZY)
    private List<Rating> ratings;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "caretaker", fetch = FetchType.LAZY)
    private List<AnimalTakenCareOf> animalsTakenCareOf;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
    private Address address;
}

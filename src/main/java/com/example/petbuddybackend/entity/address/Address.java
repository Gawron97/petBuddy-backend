package com.example.petbuddybackend.entity.address;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 6)
    private String postalCode;

    @Column(nullable = false, length = 50)
    private Voivodeship voivodeship;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false, length = 10)
    private String buildingNumber;

    @Column(length = 10)
    private String apartmentNumber;
}

package com.example.petbuddybackend.entity.address;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(nullable = false, length = 15)
    private String zipCode;

    @Column(nullable = false, length = 50)
    private Voivodeship voivodeship;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false, length = 10)
    private String streetNumber;

    @Column(length = 10)
    private String apartmentNumber;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal longitude;
}

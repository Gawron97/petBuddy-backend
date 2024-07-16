package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.address.Address;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(length = 300)
    private String description;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private Address address;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @MapsId
    @JoinColumn(name = "email")
    private AppUser accountData;
}

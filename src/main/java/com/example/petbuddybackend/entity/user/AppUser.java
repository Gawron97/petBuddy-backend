package com.example.petbuddybackend.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE)
    private Caretaker caretaker;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE)
    private Client client;
}

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
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Caretaker caretaker;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Client client;
}

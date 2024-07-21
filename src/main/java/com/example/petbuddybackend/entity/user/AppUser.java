package com.example.petbuddybackend.entity.user;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser {

    @Id @Nonnull
    private String email;
    private String name;
    private String surname;
    private String username;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE)
    private Caretaker caretaker;
}

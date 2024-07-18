package com.example.petbuddybackend.entity.user;

import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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

    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE)
    private Caretaker caretaker;
}

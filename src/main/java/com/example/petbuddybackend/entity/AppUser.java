package com.example.petbuddybackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser {

    @Id
    private String email;
    private String name;
    private String surname;
    private String username;

}

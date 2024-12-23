package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.photo.PhotoLink;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser {

    @Id
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_picture_blob", referencedColumnName = "blob")
    private PhotoLink profilePicture;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE)
    private Caretaker caretaker;

    @JoinColumn(name = "email")
    @OneToOne(mappedBy = "accountData", cascade = CascadeType.REMOVE)
    private Client client;
}

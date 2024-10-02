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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(name = "profile_picture_blob", length = 128)
    private String profilePictureBlob;

    /**
     * {@link PhotoLink} should be rather fetched using PhotoService as it will refresh the url, that is why getters and
     * setters are private. Reference to profilePictureBlob used only to ensure that profilePictureBlob is a foreign key
     * with cascade delete
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_picture_blob", referencedColumnName = "blob", insertable = false, updatable = false)
    private PhotoLink profilePicture;

    @JoinColumn(name = "email")
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Caretaker caretaker;

    @JoinColumn(name = "email")
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Client client;
}

package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.rating.Rating;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    private String email;

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "client", cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "following_caretakers",
            joinColumns = @JoinColumn(name = "client_email"),
            inverseJoinColumns = @JoinColumn(name = "caretaker_email")
    )
    @Builder.Default
    private Set<Caretaker> followingCaretakers = new HashSet<>();

    @PreUpdate
    public void preUpdate() {
        assertClientNotFollowingItself();
    }

    private void assertClientNotFollowingItself() {
        if (followingCaretakers.stream().anyMatch(caretaker -> caretaker.getEmail().equals(email))) {
            throw new IllegalStateException("Client cannot follow itself");
        }
    }

}

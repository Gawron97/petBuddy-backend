package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.notification.ClientNotification;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
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
    @OneToOne(cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "following_caretakers",
            joinColumns = @JoinColumn(name = "client_email", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "caretaker_email", nullable = false),
            uniqueConstraints = @UniqueConstraint(columnNames = {"client_email", "caretaker_email"})
    )
    @Builder.Default
    private Set<Caretaker> followingCaretakers = new HashSet<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ClientNotification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Care> cares = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void preSave() {
        assertClientNotFollowingItself();
    }

    private void assertClientNotFollowingItself() {
        if (followingCaretakers.stream().anyMatch(caretaker -> caretaker.getEmail().equals(email))) {
            throw new IllegalActionException("Client cannot follow itself");
        }
    }

}

package com.example.petbuddybackend.entity.notification;

import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long objectId;

    private String objectType;

    @Column(nullable = false, updatable = false)
    ZonedDateTime createdAt;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientEmail", nullable = false, updatable = false)
    private Client client;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        isRead = false;
    }

}

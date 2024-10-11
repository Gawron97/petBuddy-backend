package com.example.petbuddybackend.entity.notification;

import com.example.petbuddybackend.entity.user.Caretaker;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaretakerNotification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JoinColumn(name = "caretakerEmail", nullable = false, updatable = false)
    private Caretaker caretaker;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        isRead = false;
    }

}

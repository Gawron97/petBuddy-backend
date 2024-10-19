package com.example.petbuddybackend.entity.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long objectId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false, length = 100)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
        isRead = false;
    }

}
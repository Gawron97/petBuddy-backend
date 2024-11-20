package com.example.petbuddybackend.entity.care;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class CareStatusesHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private CareStatus clientStatus;

    @Column(nullable = false, updatable = false)
    private CareStatus caretakerStatus;

    @ManyToOne
    @JoinColumn(name = "care_id", nullable = false, updatable = false)
    private Care care;

    @PrePersist
    public void prePersist() {
        if(createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if(createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }

}

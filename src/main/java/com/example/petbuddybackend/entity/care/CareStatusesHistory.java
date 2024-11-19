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
    private ZonedDateTime createdAt;
    private CareStatus clientStatus;
    private CareStatus caretakerStatus;

    @ManyToOne
    @JoinColumn(name = "care_id", nullable = false, updatable = false)
    private Care care;

}

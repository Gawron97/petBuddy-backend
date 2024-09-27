package com.example.petbuddybackend.entity.photo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "blob")
@EntityListeners(CloudPhotoDeleteListener.class)
public class CloudPhoto {

    @Id
    @Column(nullable = false, length = 64, unique = true)
    private String blob;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false)
    private LocalDateTime urlExpiresAt;
}

package com.example.petbuddybackend.entity.block;

import com.example.petbuddybackend.entity.user.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(BlockId.class)
@Check(constraints = "blocker_email != blocked_email")
public class Block {

    @Id
    private String blockerEmail;

    @Id
    private String blockedEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime blockDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blockerEmail", referencedColumnName = "email", nullable = false, insertable = false, updatable = false)
    private AppUser blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blockedEmail", referencedColumnName = "email", nullable = false, insertable = false, updatable = false)
    private AppUser blocked;

    public Block(String blockerEmail, String blockedEmail) {
        this.blockerEmail = blockerEmail;
        this.blockedEmail = blockedEmail;
    }

    @PrePersist
    public void prePersist() {
        blockDate = LocalDateTime.now();
    }
}

package com.example.petbuddybackend.entity.chat;

import com.example.petbuddybackend.entity.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(indexes = @Index(name = "chatRoomCreatedAtIndex", columnList = "chat_room_id, createdAt DESC"))
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000, updatable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chatRoomId", referencedColumnName = "id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "senderEmail", referencedColumnName = "email")
    private AppUser sender;

    @Builder.Default
    @Column(nullable = false)
    private Boolean seenByRecipient = false;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }

        if (seenByRecipient == null) {
            seenByRecipient = false;
        }
    }
}

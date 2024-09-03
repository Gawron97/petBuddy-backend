package com.example.petbuddybackend.entity.chat;

import com.example.petbuddybackend.entity.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Builder
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chatRoomId", referencedColumnName = "id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "senderEmail", referencedColumnName = "email")
    private AppUser sender;

    @Transient
    public boolean isSeenByRecipient() {
        AppUser clientAccountData = chatRoom.getClient().getAccountData();
        AppUser caretakerAccountData = chatRoom.getCaretaker().getAccountData();

        if(sender.equals(clientAccountData)) {
            ChatMessage lastSeenByCaretaker = chatRoom.getLastMessageSeenByCaretaker();
            return lastSeenByCaretaker != null && !lastSeenByCaretaker.getCreatedAt().isBefore(createdAt);
        } else if(sender.equals(caretakerAccountData)) {
            ChatMessage lastSeenByClient = chatRoom.getLastMessageSeenByClient();
            return lastSeenByClient != null && !lastSeenByClient.getCreatedAt().isBefore(createdAt);
        }

        return false;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}

package com.example.petbuddybackend.entity.chat;

import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Formula;

import java.util.List;

@Entity
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"clientEmail", "caretakerEmail"})}
)
@Check(constraints = "client_email <> caretaker_email")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clientEmail", referencedColumnName = "email")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caretakerEmail", referencedColumnName = "email")
    private Caretaker caretaker;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ChatMessage> messages;

    @Basic(fetch = FetchType.LAZY)
    @Formula("(SELECT cm.id FROM chat_message cm WHERE cm.chat_room_id = id ORDER BY cm.created_at DESC LIMIT 1)")
    private Long lastMessageId;

    @OneToOne(fetch = FetchType.LAZY)
    private ChatMessage lastMessageSeenByClient;

    @OneToOne(fetch = FetchType.LAZY)
    private ChatMessage lastMessageSeenByCaretaker;
}

package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByIdAndCaretaker_Email(Long chatRoomId, String email);

    boolean existsByIdAndClient_Email(Long chatRoomId, String email);

    boolean existsByClient_EmailAndCaretaker_Email(String clientEmail, String caretakerEmail);

    Optional<ChatRoom> findByClient_EmailAndCaretaker_Email(String clientEmail, String caretakerEmail);

    @Query("""
        SELECT COUNT(cr)
        FROM ChatRoom cr
        WHERE cr.client.email = :userEmail
        AND EXISTS(
            SELECT cm
            FROM ChatMessage cm
            WHERE cm.chatRoom.id = cr.id
                AND cm.sender.email != :userEmail
                AND cm.seenByRecipient = false
        )
    """)
    Integer countUnreadChatsForUserAsClient(String userEmail);

    @Query("""
        SELECT COUNT(cr)
        FROM ChatRoom cr
        WHERE cr.caretaker.email = :userEmail
        AND EXISTS(
            SELECT cm
            FROM ChatMessage cm
            WHERE cm.chatRoom.id = cr.id
                AND cm.sender.email != :userEmail
                AND cm.seenByRecipient = false
        )
    """)
    Integer countUnreadChatsForUserAsCaretaker(String userEmail);
}

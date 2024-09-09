package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByIdAndCaretaker_Email(Long chatRoomId, String email);

    boolean existsByIdAndClient_Email(Long chatRoomId, String email);

    boolean existsByClient_EmailAndCaretaker_Email(String clientEmail, String caretakerEmail);

    @Query("""
        SELECT new com.example.petbuddybackend.dto.chat.ChatRoomDTO(
               cr.id,
               cr.caretaker.email,
               cr.caretaker.accountData.name,
               cr.caretaker.accountData.surname,
               cm.createdAt,
               cm.content,
               cm.sender.email,
               CASE WHEN cm.sender.email = cr.client.email THEN true ELSE cm.seenByRecipient END
        )
        FROM ChatRoom cr
        JOIN ChatMessage cm ON cm.chatRoom.id = cr.id
        WHERE cr.client.email = :email
        AND cm.id = (
            SELECT MIN(cm2.id)
            FROM ChatMessage cm2
            WHERE cm2.createdAt = (
                SELECT MAX(cm3.createdAt)
                FROM ChatMessage cm3
                WHERE cm3.chatRoom.id = cr.id
            ) AND cm2.chatRoom.id = cr.id
        )
        """)
    Page<ChatRoomDTO> findByClientEmailSortByLastMessageDesc(String email, Pageable pageable);

    @Query("""
        SELECT new com.example.petbuddybackend.dto.chat.ChatRoomDTO(
               cr.id,
               cr.client.email,
               cr.client.accountData.name,
               cr.client.accountData.surname,
               cm.createdAt,
               cm.content,
               cm.sender.email,
               CASE WHEN cm.sender.email = cr.caretaker.email THEN true ELSE cm.seenByRecipient END
        )
        FROM ChatRoom cr
        JOIN ChatMessage cm ON cm.chatRoom.id = cr.id
        WHERE cr.caretaker.email = :email
        AND cm.id = (
            SELECT MIN(cm2.id)
            FROM ChatMessage cm2
            WHERE cm2.createdAt = (
                SELECT MAX(cm3.createdAt)
                FROM ChatMessage cm3
                WHERE cm3.chatRoom.id = cr.id
            ) AND cm2.chatRoom.id = cr.id
        )
        """)
    Page<ChatRoomDTO> findByCaretakerEmailSortByLastMessageDesc(String email, Pageable pageable);
}

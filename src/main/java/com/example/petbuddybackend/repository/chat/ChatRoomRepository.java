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

    @Query(value = """
        SELECT new com.example.petbuddybackend.dto.chat.ChatRoomDTO(
               cr.id,
               cr.client.email,
               cr.client.accountData.name,
               cr.client.accountData.surname,
               cm.createdAt,
               cm.content
        )
        FROM ChatRoom cr
        JOIN ChatMessage cm ON cm.chatRoom.id = cr.id
        WHERE cr.client.email = :email
        AND cm.createdAt = (
            SELECT MAX(cm2.createdAt)
            FROM ChatMessage cm2
            WHERE cm2.chatRoom.id = cr.id
        )
        """)
    Page<ChatRoomDTO> findByCaretakerEmailSortByLastMessageDesc(String email, Pageable pageable);

    @Query(value = """
        SELECT new com.example.petbuddybackend.dto.chat.ChatRoomDTO(
               cr.id,
               cr.caretaker.email,
               cr.caretaker.accountData.name,
               cr.caretaker.accountData.surname,
               cm.createdAt,
               cm.content
        )
        FROM ChatRoom cr
        JOIN ChatMessage cm ON cm.chatRoom.id = cr.id
        WHERE cr.client.email = :email
        AND cm.createdAt = (
            SELECT MAX(cm2.createdAt)
            FROM ChatMessage cm2
            WHERE cm2.chatRoom.id = cr.id
        )
        """)
    Page<ChatRoomDTO> findByClientEmailSortByLastMessageDesc(String email, Pageable pageable);
}

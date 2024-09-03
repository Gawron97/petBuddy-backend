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
            (lmsbc IS NOT NULL AND lmsbc.createdAt >= cm.createdAt)
        )
        FROM ChatRoom cr
        JOIN ChatMessage cm ON cr.lastMessageId = cm.id
        LEFT JOIN ChatMessage lmsbc ON lmsbc.id = cr.lastMessageSeenByClient.id
        WHERE cr.client.email = :email
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
            (lmsbc IS NOT NULL AND lmsbc.createdAt >= cm.createdAt)
        )
        FROM ChatRoom cr
        JOIN ChatMessage cm ON cr.lastMessageId = cm.id
        LEFT JOIN ChatMessage lmsbc ON lmsbc.id = cr.lastMessageSeenByCaretaker.id
        WHERE cr.caretaker.email = :email
        """)
    Page<ChatRoomDTO> findByCaretakerEmailSortByLastMessageDesc(String email, Pageable pageable);
}

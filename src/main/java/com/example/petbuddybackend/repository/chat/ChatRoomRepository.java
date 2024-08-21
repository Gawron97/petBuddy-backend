package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
                    SELECT CASE
                        WHEN COUNT(c) > 0 THEN TRUE
                        ELSE FALSE
                    END
                    FROM ChatRoom c
                    WHERE c.id = :chatRoomId AND c.caretaker.email = :email
            """)
    boolean caretakerParticipatesInChat(Long chatRoomId, String email);

    @Query("""
                    SELECT CASE
                        WHEN COUNT(c) > 0 THEN TRUE
                        ELSE FALSE
                    END
                    FROM ChatRoom c
                    WHERE c.id = :chatRoomId AND c.client.email = :email
            """)
    boolean clientParticipatesInChat(Long chatRoomId, String email);
}

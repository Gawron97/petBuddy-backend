package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByIdAndCaretaker_Email(Long chatRoomId, String email);

    boolean existsByIdAndClient_Email(Long chatRoomId, String email);

    boolean existsByClient_EmailAndCaretaker_Email(String clientEmail, String caretakerEmail);
}

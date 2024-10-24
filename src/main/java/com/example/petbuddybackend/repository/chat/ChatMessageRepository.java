package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatRoom_Id_OrderByCreatedAtDesc(Long chatId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatMessage m
        SET m.seenByRecipient = true
        WHERE m.chatRoom.id = :chatRoomId
          AND m.seenByRecipient = false
          AND m.sender.email != :caretakerEmail
        """)
    void updateUnseenMessagesOfCaretaker(Long chatRoomId, String caretakerEmail);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatMessage m
        SET m.seenByRecipient = true
        WHERE m.chatRoom.id = :chatRoomId
          AND m.seenByRecipient = false
          AND m.sender.email != :clientEmail
        """)
    void updateUnseenMessagesOfClient(Long chatRoomId, String clientEmail);

    ChatMessage findFirstByChatRoom_IdOrderByCreatedAtDesc(Long chatRoomId);
}

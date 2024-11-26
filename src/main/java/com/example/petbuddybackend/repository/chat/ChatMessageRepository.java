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
          AND m.sender.email != :userEmail
    """)
    void updateUnseenMessagesOfUser(Long chatRoomId, String userEmail);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatMessage m
        SET m.seenByRecipient = true
        WHERE m.chatRoom.id = :chatRoomId
          AND m.seenByRecipient = false
        """)
    void updateMessagesSeenOfBothUsers(Long chatRoomId);

    ChatMessage findFirstByChatRoom_IdOrderByCreatedAtDesc(Long chatRoomId);

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        WHERE cm.chatRoom.client.email = :clientUsername
            AND cm.createdAt = (
                SELECT MAX(cm2.createdAt)
                FROM ChatMessage cm2
                WHERE cm2.chatRoom = cm.chatRoom
            )
        ORDER BY cm.createdAt DESC
    """)
    Page<ChatMessage> findLastMessagesOfChatRoomsOfClient(String clientUsername, Pageable pageable);

    @Query("""
        SELECT cm
        FROM ChatMessage cm
        WHERE cm.chatRoom.caretaker.email = :caretakerUsername
            AND cm.createdAt = (
                SELECT MAX(cm2.createdAt)
                FROM ChatMessage cm2
                WHERE cm2.chatRoom = cm.chatRoom
            )
        ORDER BY cm.createdAt DESC
    """)
    Page<ChatMessage> findLastMessagesOfChatRoomsOfCaretaker(String caretakerUsername, Pageable pageable);
}

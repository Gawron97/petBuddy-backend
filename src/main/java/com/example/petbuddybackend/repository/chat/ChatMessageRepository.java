package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatRoom_Id_OrderByCreatedAtDesc(Long chatId, Pageable pageable);

    Optional<ChatMessage> findFirstByChatRoom_IdOrderByCreatedAtDesc(Long id);
}

package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.NotParticipateException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final ChatMapper chatMapper = ChatMapper.INSTANCE;

    public Page<ChatMessageDTO> getChatMessages(Long chatId, String principalEmail, Pageable pageable) {
        checkChatExistsById(chatId);
        checkUserParticipatesInChat(chatId, principalEmail);

        return chatMessageRepository.findByChatRoom_Id_OrderByCreatedAtDesc(chatId, pageable)
                .map(chatMapper::mapToChatMessageDTO);
    }

    public Page<ChatMessageDTO> getChatMessages(Long chatId, String principalEmail, Pageable pageable, ZoneId timeZone) {
        return getChatMessages(chatId, principalEmail, pageable)
                .map(message -> convertTimeZone(message, timeZone));
    }

    private void checkChatExistsById(Long chatId) {
        if(!chatRepository.existsById(chatId)) {
            throw new NotFoundException("Chat with id: " + chatId + " not found");
        }
    }

    private void checkUserParticipatesInChat(Long chatId, String email) {
        if(!isUserInChat(chatId, email)) {
            throw new NotParticipateException("User with email: " + email + " is not in chat of id " + chatId);
        }
    }

    private boolean isUserInChat(Long chatId, String principal) {
        return chatRepository.existsByIdAndClient_Email(chatId, principal) ||
                chatRepository.existsByIdAndCaretaker_Email(chatId, principal);
    }

    private ChatMessageDTO convertTimeZone(ChatMessageDTO message, ZoneId timeZone) {
        ZonedDateTime zonedDateTime = message.getCreatedAt()
                .withZoneSameInstant(timeZone);

        message.setCreatedAt(zonedDateTime);
        return message;
    }
}

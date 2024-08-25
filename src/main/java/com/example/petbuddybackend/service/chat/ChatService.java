package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Role;
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

    private static final String CHAT = "Chat";
    private static final String PARTICIPATE_EXCEPTION_MESSAGE = "User with email: %s is not in chat of id %s";

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

    public ChatMessageDTO saveMessage(Long chatId, String principalEmail, ChatMessageSent chatMessage, Role role) {
        ChatRoom chatRoom = getChatRoomById(chatId);
        checkUserParticipatesInChat(chatRoom, principalEmail, role);

        AppUser sender = role == Role.CLIENT ?
                chatRoom.getClient().getAccountData():
                chatRoom.getCaretaker().getAccountData();

        return chatMapper.mapToChatMessageDTO(saveMessage(chatRoom, sender, chatMessage.content()));
    }

    public ChatMessageDTO saveMessage(Long chatId, String principalEmail, ChatMessageSent chatMessage, Role role, ZoneId timeZone) {
        return convertTimeZone(
                saveMessage(chatId, principalEmail, chatMessage, role),
                timeZone
        );
    }

    public ChatRoom getChatRoomById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(chatId.toString(), CHAT));
    }

    private ChatMessage saveMessage(ChatRoom chatRoom, AppUser sender, String content) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .content(content)
                .chatRoom(chatRoom)
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    private void checkChatExistsById(Long chatId) {
        if(!chatRepository.existsById(chatId)) {
            throw NotFoundException.withFormattedMessage(chatId.toString(), CHAT);
        }
    }

    private void checkUserParticipatesInChat(Long chatId, String email) {
        if(!isUserInChat(chatId, email)) {
            throw new NotParticipateException(String.format(PARTICIPATE_EXCEPTION_MESSAGE, email, chatId));
        }
    }

    private void checkUserParticipatesInChat(ChatRoom chatRoom, String email, Role role) {
        if(!isUserInChat(chatRoom, email, role)) {
            throw new NotParticipateException(String.format(PARTICIPATE_EXCEPTION_MESSAGE, email, chatRoom.getId()));
        }
    }

    private boolean isUserInChat(Long chatId, String email) {
        return chatRepository.existsByIdAndClient_Email(chatId, email) ||
                chatRepository.existsByIdAndCaretaker_Email(chatId, email);
    }

    private boolean isUserInChat(ChatRoom chatRoom, String email, Role role) {
        return role == Role.CLIENT ?
                chatRoom.getClient().getEmail().equals(email) :
                chatRoom.getCaretaker().getEmail().equals(email);
    }

    private ChatMessageDTO convertTimeZone(ChatMessageDTO message, ZoneId timeZone) {
        ZonedDateTime zonedDateTime = message.getCreatedAt()
                .withZoneSameInstant(timeZone);

        message.setCreatedAt(zonedDateTime);
        return message;
    }
}

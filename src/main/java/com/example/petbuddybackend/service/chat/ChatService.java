package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.service.mapper.ChatMapper;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.service.user.ClientService;
import com.example.petbuddybackend.utils.exception.throweable.chat.ChatAlreadyExistsException;
import com.example.petbuddybackend.utils.exception.throweable.chat.InvalidMessageReceiverException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CHAT = "Chat";
    private static final String PARTICIPATE_EXCEPTION_MESSAGE = "User with email: %s is not in chat of id %s";
    private static final String CHAT_PARTICIPANTS_ALREADY_EXIST_MESSAGE = "Chat between client: \"%s\" and caretaker \"%s\" already exists";

    private final ClientService clientService;
    private final CaretakerService caretakerService;
    private final ChatRoomRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMapper chatMapper = ChatMapper.INSTANCE;

    public Page<ChatMessageDTO> getChatMessages(Long chatId, String principalEmail, Pageable pageable, ZoneId timeZone) {
        checkChatExistsById(chatId);
        checkUserParticipatesInChat(chatId, principalEmail);

        return chatMessageRepository.findByChatRoom_Id_OrderByCreatedAtDesc(chatId, pageable)
                .map(msg -> chatMapper.mapToChatMessageDTO(msg, timeZone));
    }

    public ChatRoom getChatRoomById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(chatId.toString(), CHAT));
    }

    public Page<ChatRoomDTO> getChatRoomsByParticipantEmail(
            String principalEmail,
            Role role,
            Pageable pageable,
            ZoneId timeZone
    ) {
        Page<ChatRoomDTO> chatRoomDTOS = role == Role.CLIENT ?
                chatRepository.findByClientEmailSortByLastMessageDesc(principalEmail, pageable) :
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(principalEmail, pageable);

        return chatRoomDTOS.map(room -> chatMapper.mapTimeZone(room, timeZone));
    }

    @Transactional
    public ChatMessageDTO createMessage(
            Long chatId,
            String principalEmail,
            ChatMessageSent chatMessage,
            Role role
    ) {
        return chatMapper.mapToChatMessageDTO(createMessage(chatId, principalEmail, role, chatMessage));
    }

    public ChatMessageDTO createChatRoomWithMessage(
            String messageReceiverEmail,
            String principalEmail,
            Role principalRole,
            ChatMessageSent message,
            ZoneId timeZone
    ) {
        checkSenderIsNotTheSameAsReceiver(principalEmail, messageReceiverEmail);
        checkChatNotExistsByParticipants(messageReceiverEmail, principalEmail, principalRole);

        return principalRole == Role.CLIENT ?
                createChatRoomWithMessageForClientSender(principalEmail, messageReceiverEmail, message, timeZone) :
                createChatRoomWithMessageForCaretakerSender(principalEmail, messageReceiverEmail, message, timeZone);
    }

    public boolean isUserInChat(Long chatId, String email) {
        return chatRepository.existsByIdAndClient_Email(chatId, email) ||
                chatRepository.existsByIdAndCaretaker_Email(chatId, email);
    }

    public boolean isUserInChat(ChatRoom chatRoom, String email, Role role) {
        return role == Role.CLIENT ?
                chatRoom.getClient().getEmail().equals(email) :
                chatRoom.getCaretaker().getEmail().equals(email);
    }

    private ChatMessageDTO createChatRoomWithMessageForClientSender(
            String clientSenderEmail,
            String caretakerReceiverEmail,
            ChatMessageSent message,
            ZoneId timeZone
    ) {
        Client clientSender = clientService.getClientByEmail(clientSenderEmail);
        Caretaker caretakerReceiver = caretakerService.getCaretakerByEmail(caretakerReceiverEmail);
        ChatRoom chatRoom = createChatRoom(clientSender, caretakerReceiver);
        ChatMessage chatMessage = createMessage(chatRoom, clientSender.getAccountData(), message.getContent());

        return chatMapper.mapToChatMessageDTO(chatMessageRepository.save(chatMessage), timeZone);
    }

    private ChatMessageDTO createChatRoomWithMessageForCaretakerSender(
            String caretakerSenderEmail,
            String clientReceiverEmail,
            ChatMessageSent message,
            ZoneId timeZone
    ) {
        Client clientReceiver = clientService.getClientByEmail(clientReceiverEmail);
        Caretaker caretakerSender = caretakerService.getCaretakerByEmail(caretakerSenderEmail);
        ChatRoom chatRoom = createChatRoom(clientReceiver, caretakerSender);
        ChatMessage chatMessage = createMessage(chatRoom, caretakerSender.getAccountData(), message.getContent());

        return chatMapper.mapToChatMessageDTO(chatMessageRepository.save(chatMessage), timeZone);
    }

    private ChatMessage createMessage(Long chatId, String principalEmail, Role principalRole, ChatMessageSent chatMessage) {
        ChatRoom chatRoom = getChatRoomById(chatId);
        checkUserParticipatesInChat(chatRoom, principalEmail, principalRole);

        AppUser sender = principalRole == Role.CLIENT ?
                chatRoom.getClient().getAccountData() :
                chatRoom.getCaretaker().getAccountData();

        return createMessage(chatRoom, sender, chatMessage.getContent());
    }

    private ChatMessage createMessage(ChatRoom chatRoom, AppUser sender, String content) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .content(content)
                .chatRoom(chatRoom)
                .build();

        return chatMessageRepository.save(chatMessage);
    }

    private ChatRoom createChatRoom(Client client, Caretaker caretaker) {
        ChatRoom chatRoom = ChatRoom.builder()
                .client(client)
                .caretaker(caretaker)
                .build();

        return chatRepository.save(chatRoom);
    }

    private void checkChatExistsById(Long chatId) {
        if(!chatRepository.existsById(chatId)) {
            throw NotFoundException.withFormattedMessage(CHAT, chatId.toString());
        }
    }

    private void checkChatNotExistsByParticipants(String principalEmail, String otherParticipantEmail, Role principalRole) {
        if(principalRole == Role.CLIENT) {
            checkChatNotExistsByParticipants(otherParticipantEmail, principalEmail);
        } else {
            checkChatNotExistsByParticipants(principalEmail, otherParticipantEmail);
        }
    }

    private void checkChatNotExistsByParticipants(String clientEmail, String caretakerEmail) {
        if(chatRepository.existsByClient_EmailAndCaretaker_Email(clientEmail, caretakerEmail)) {
            throw new ChatAlreadyExistsException(String.format(CHAT_PARTICIPANTS_ALREADY_EXIST_MESSAGE, clientEmail, caretakerEmail));
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

    private void checkSenderIsNotTheSameAsReceiver(String senderEmail, String receiverEmail) {
        if(senderEmail.equals(receiverEmail)) {
            throw new InvalidMessageReceiverException("Unable to send message to yourself");
        }
    }
}

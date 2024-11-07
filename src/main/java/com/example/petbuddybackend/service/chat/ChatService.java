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
    private static final String SENT_TO_YOURSELF_MESSAGE = "Unable to send message to yourself";
    private static final String INVALID_USER_ROLE_MESSAGE = "Invalid user role: %s";

    private final ClientService clientService;
    private final CaretakerService caretakerService;
    private final ChatRoomRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMapper chatMapper = ChatMapper.INSTANCE;
    private final ChatRoomRepository chatRoomRepository;

    public Page<ChatMessageDTO> getChatMessagesByParticipantEmail(
            Long chatId,
            String principalEmail,
            Pageable pageable,
            ZoneId timeZone
    ) {
        checkChatExists(chatId);
        checkUserInChat(chatId, principalEmail);

        return chatMessageRepository
                .findByChatRoom_Id_OrderByCreatedAtDesc(chatId, pageable)
                .map(message -> chatMapper.mapToChatMessageDTO(message, timeZone));
    }

    public ChatRoom getChatRoomById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CHAT, chatId.toString()));
    }

    public Page<ChatRoomDTO> getChatRoomsByParticipantEmail(
            String principalEmail,
            Role role,
            Pageable pageable,
            ZoneId timeZone
    ) {
        Page<ChatRoomDTO> chatRoomDTOs = role == Role.CLIENT ?
                chatRepository.findByClientEmailSortByLastMessageDesc(principalEmail, pageable) :
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(principalEmail, pageable);

        return chatRoomDTOs.map(room -> chatMapper.mapTimeZone(room, timeZone));
    }

    @Transactional
    public ChatMessageDTO createMessage(
            ChatRoom chatRoom,
            String senderEmail,
            Role senderRole,
            ChatMessageSent chatMessage,
            boolean seenByRecipient
    ) {
        checkUserInChat(chatRoom, senderEmail, senderRole);

        AppUser sender = senderRole.equals(Role.CLIENT) ?
                chatRoom.getClient().getAccountData() :
                chatRoom.getCaretaker().getAccountData();

        ChatMessage persistedMessage = persistMessage(chatRoom, sender, chatMessage.getContent(), seenByRecipient);
        performLastMessageSeenUpdate(chatRoom.getId(), senderEmail, senderRole, seenByRecipient);
        return chatMapper.mapToChatMessageDTO(persistedMessage);
    }

    @Transactional
    public void markMessagesAsSeen(Long chatId, String username) {
        ChatRoom chatRoom = getChatRoomById(chatId);
        checkUserInChat(chatId, username);

        if(chatRoom.getClient().getEmail().equals(username)) {
            chatMessageRepository.updateUnseenMessagesOfClient(chatId, username);
        } else {
            chatMessageRepository.updateUnseenMessagesOfCaretaker(chatId, username);
        }
    }

    @Transactional
    public ChatMessageDTO createChatRoomWithMessage(
            String principalEmail,
            Role principalRole,
            String messageReceiverEmail,
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

    public boolean isUserInChat(Long chatId, String email, Role role) {
        return role == Role.CLIENT ?
                chatRepository.existsByIdAndClient_Email(chatId, email) :
                chatRepository.existsByIdAndCaretaker_Email(chatId, email);
    }

    public boolean isUserInChat(ChatRoom chatRoom, String email, Role role) {
        return role == Role.CLIENT ?
                chatRoom.getClient().getEmail().equals(email) :
                chatRoom.getCaretaker().getEmail().equals(email);
    }

    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoomWithParticipant(
            String username,
            Role userRole,
            String participantUsername,
            ZoneId timeZone
    ) {
        ChatRoom chatRoom;
        AppUser chatter;

        switch(userRole) {
            case CLIENT:
                chatRoom = getByClientEmailAndCaretakerEmail(username, participantUsername);
                chatter = chatRoom.getCaretaker().getAccountData();
                break;
            case CARETAKER:
                chatRoom = getByClientEmailAndCaretakerEmail(participantUsername, username);
                chatter = chatRoom.getClient().getAccountData();
                break;
            default:
                throw new UnsupportedOperationException(String.format(INVALID_USER_ROLE_MESSAGE, userRole));
        }

        ChatMessage lastMessage = chatMessageRepository.findFirstByChatRoom_IdOrderByCreatedAtDesc(chatRoom.getId());
        boolean isSeenByPrincipal = messageSeenByPrincipal(username, lastMessage);
        return chatMapper.mapToChatRoomDTO(chatRoom.getId(), chatter, lastMessage, isSeenByPrincipal, timeZone);
    }

    private void performLastMessageSeenUpdate(Long chatId, String senderEmail, Role senderRole, boolean seenByRecipient) {
        if(seenByRecipient) {
            chatMessageRepository.updateMessageSeenOfBothUsers(chatId);
        } else {
            if(senderRole == Role.CLIENT) {
                chatMessageRepository.updateUnseenMessagesOfCaretaker(chatId, senderEmail);
            } else {
                chatMessageRepository.updateUnseenMessagesOfClient(chatId, senderEmail);
            }
        }
    }

    private boolean messageSeenByPrincipal(String username, ChatMessage lastMessage) {
        String senderEmail = lastMessage.getSender().getEmail();

        if(senderEmail.equals(username)) {
            return true;
        }

        return lastMessage.getSeenByRecipient();
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
        ChatMessage chatMessage = persistMessage(
                chatRoom,
                clientSender.getAccountData(),
                message.getContent(),
                false
        );

        chatMessageRepository.updateUnseenMessagesOfClient(chatRoom.getId(), clientSenderEmail);

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
        ChatMessage chatMessage = persistMessage(
                chatRoom,
                caretakerSender.getAccountData(),
                message.getContent(),
                false
        );

        chatMessageRepository.updateUnseenMessagesOfCaretaker(chatRoom.getId(), caretakerSenderEmail);

        return chatMapper.mapToChatMessageDTO(chatMessageRepository.save(chatMessage), timeZone);
    }

    private ChatMessage persistMessage(ChatRoom chatRoom, AppUser sender, String content, boolean seenByRecipient) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .content(content)
                .seenByRecipient(seenByRecipient)
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

    private void checkChatExists(Long chatId) {
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

    private ChatRoom getByClientEmailAndCaretakerEmail(String clientEmail, String caretakerEmail) {
        return chatRoomRepository.findByClient_EmailAndCaretaker_Email(clientEmail, caretakerEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(
                        CHAT,
                        String.format("client: %s, caretaker: %s", clientEmail, caretakerEmail))
                );
    }

    private void checkUserInChat(Long chatId, String email) {
        if(!isUserInChat(chatId, email)) {
            throw new NotParticipateException(String.format(PARTICIPATE_EXCEPTION_MESSAGE, email, chatId));
        }
    }

    private void checkUserInChat(ChatRoom chatRoom, String email, Role role) {
        if(!isUserInChat(chatRoom, email, role)) {
            throw new NotParticipateException(String.format(PARTICIPATE_EXCEPTION_MESSAGE, email, chatRoom.getId()));
        }
    }

    private Role getRoleOfUserInChat(Long chatId, String email) {
        if(chatRepository.existsByIdAndClient_Email(chatId, email)) {
            return Role.CLIENT;
        }
        else if(chatRepository.existsByIdAndCaretaker_Email(chatId, email)) {
            return Role.CARETAKER;
        }

        throw new NotParticipateException(String.format(PARTICIPATE_EXCEPTION_MESSAGE, email, chatId));
    }

    private void checkSenderIsNotTheSameAsReceiver(String senderEmail, String receiverEmail) {
        if(senderEmail.equals(receiverEmail)) {
            throw new InvalidMessageReceiverException(SENT_TO_YOURSELF_MESSAGE);
        }
    }
}

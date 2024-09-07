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
import com.example.petbuddybackend.service.chat.session.MessageCallback;
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

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CHAT = "Chat";
    private static final String PARTICIPATE_EXCEPTION_MESSAGE = "User with email: %s is not in chat of id %s";
    private static final String CHAT_PARTICIPANTS_ALREADY_EXIST_MESSAGE = "Chat between client: \"%s\" and caretaker \"%s\" already exists";
    private static final String SENT_TO_YOURSELF_MESSAGE = "Unable to send message to yourself";

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
        checkChatExistsById(chatId);
        checkUserInChat(chatId, principalEmail);

        Page<ChatMessage> chatMessages = chatMessageRepository.findByChatRoom_Id_OrderByCreatedAtDesc(chatId, pageable);
        return chatMessages.map(message -> chatMapper.mapToChatMessageDTO(message, timeZone));
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
        Page<ChatRoomDTO> chatRoomDTOs = role == Role.CLIENT ?
                chatRepository.findByClientEmailSortByLastMessageDesc(principalEmail, pageable) :
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(principalEmail, pageable);

        return chatRoomDTOs.map(room -> chatMapper.mapTimeZone(room, timeZone));
    }

    /**
     * Creates a message in the chat room with the given id and updates the last message seen by the user in the chat room.
     * */
    public ChatMessageDTO createMessage(
            Long chatId,
            String principalEmail,
            ChatMessageSent chatMessage,
            Role role
    ) {
        ChatMessage message = createMessageForRoleAndUpdateSeen(chatId, principalEmail, role, chatMessage);
        return chatMapper.mapToChatMessageDTO(message);
    }

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

    public boolean isUserInChat(ChatRoom chatRoom, String email, Role role) {
        return role == Role.CLIENT ?
                chatRoom.getClient().getEmail().equals(email) :
                chatRoom.getCaretaker().getEmail().equals(email);
    }

    /**
     * Updates the last message seen by the user in the chat room to the latest message in the chat room.
     * */
    public void updateLastMessageSeen(Long chatId, String email) {
        Role userRole = getRoleOfUserInChat(chatId, email);

        if(userRole == Role.CLIENT) {
            chatMessageRepository.updateUnseenMessagesOfClient(chatId, email);
        } else {
            chatMessageRepository.updateUnseenMessagesOfCaretaker(chatId, email);
        }
    }

    public MessageCallback createCallbackMessageSeen(Long chatId, String usernameToSkip) {
        return usernameSend -> {
            if(!usernameSend.equals(usernameToSkip)) {
                updateLastMessageSeen(chatId, usernameSend);
            }
        };
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
        ChatMessage chatMessage = persistMessage(chatRoom, clientSender.getAccountData(), message.getContent());

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
        ChatMessage chatMessage = persistMessage(chatRoom, caretakerSender.getAccountData(), message.getContent());

        chatMessageRepository.updateUnseenMessagesOfCaretaker(chatRoom.getId(), caretakerSenderEmail);

        return chatMapper.mapToChatMessageDTO(chatMessageRepository.save(chatMessage), timeZone);
    }

    private ChatMessage createMessageForRoleAndUpdateSeen(Long chatId, String principalEmail, Role principalRole, ChatMessageSent chatMessage) {
        ChatRoom chatRoom = getChatRoomById(chatId);
        checkUserInChat(chatRoom, principalEmail, principalRole);

        if(principalRole == Role.CLIENT) {
            AppUser sender = chatRoom.getClient().getAccountData();
            ChatMessage message = persistMessage(chatRoom, sender, chatMessage.getContent());
            chatMessageRepository.updateUnseenMessagesOfClient(chatId, sender.getEmail());
            chatRoomRepository.save(chatRoom);
            return message;
        } else {
            AppUser sender = chatRoom.getCaretaker().getAccountData();
            ChatMessage message = persistMessage(chatRoom, sender, chatMessage.getContent());
            chatMessageRepository.updateUnseenMessagesOfCaretaker(chatId, sender.getEmail());
            chatRoomRepository.save(chatRoom);
            return message;
        }
    }

    private ChatMessage persistMessage(ChatRoom chatRoom, AppUser sender, String content) {
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

package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.dto.notification.UnseenChatsNotificationDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import com.example.petbuddybackend.utils.exception.throweable.chat.ChatAlreadyExistsException;
import com.example.petbuddybackend.utils.exception.throweable.chat.InvalidMessageReceiverException;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.example.petbuddybackend.testutils.mock.MockChatProvider.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChatServiceTest {

    private static Client client;
    private static Client otherClientWithCaretakerAccount;
    private static Caretaker caretaker;
    private static Caretaker otherCaretaker;
    private static Caretaker otherCaretakerWithClientAccount;

    @Autowired
    private ChatRoomRepository chatRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker()
        );

        client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient()
        );

        chatRoom = PersistenceUtils.addChatRoom(
                createMockChatRoom(client, caretaker),
                MockChatProvider.createMockChatMessages(client, caretaker),
                chatRepository,
                chatMessageRepository
        );

        otherCaretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker("newCaretakerEmail")
        );

        // Add user that is both a client and a caretaker
        otherClientWithCaretakerAccount = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient("newClientEmail")
        );

        otherCaretakerWithClientAccount = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker("newClientEmail")
        );
    }

    @AfterEach
    void tearDown() {
        chatRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testGetChatMessages_shouldSucceed() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ChatMessageDTO> clientChatMessages = chatService.getChatMessagesByParticipantEmail(
                chatRoom.getId(),
                client.getEmail(),
                pageable,
                ZoneId.of("Europe/Warsaw")
        );
        Page<ChatMessageDTO> caretakerChatMessages = chatService.getChatMessagesByParticipantEmail(
                chatRoom.getId(),
                caretaker.getEmail(),
                pageable,
                ZoneId.of("Europe/Warsaw")
        );

        assertEquals(clientChatMessages, caretakerChatMessages);
        assertEquals(2, clientChatMessages.getContent().size());
    }

    @ParameterizedTest
    @MethodSource("provideTimeZones")
    void testGetChatMessages_changeTimeZone_shouldReturnCorrectTime(String timeZone) {
        Pageable pageable = PageRequest.of(0, 10);
        ZoneId zoneId = ZoneId.of(timeZone);

        Page<ChatMessageDTO> clientChatMessages = chatService.getChatMessagesByParticipantEmail(chatRoom.getId(), client.getEmail(), pageable, zoneId);

        for(ChatMessageDTO chatMessageDTO : clientChatMessages.getContent()) {
            assertEquals(zoneId, chatMessageDTO.getCreatedAt().getZone());
        }
    }

    @Test
    void testGetChatMessages_chatDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> chatService.getChatMessagesByParticipantEmail(-1L, "", null, ZoneId.of("Europe/Warsaw"))
        );
    }

    @Test
    void testGetChatMessages_userDoesNotParticipateChat_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.getChatMessagesByParticipantEmail(chatRoom.getId(), "notAParticipant", null, ZoneId.of("Europe/Warsaw"))
        );
    }

    @Test
    void testGetChatRoomById_shouldSucceed() {
        ChatRoom chatRoomById = chatService.getChatRoomById(chatRoom.getId());
        assertEquals(chatRoom, chatRoomById);
    }

    @Test
    void testGetChatRoomById_chatDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> chatService.getChatRoomById(-1L)
        );
    }

    @Test
    void testGetChatRoomsByParticipantEmail_shouldSucceed() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ChatRoomDTO> clientChatRooms = chatService.getChatRoomsByCriteriaSortedByLastMessage(
                client.getEmail(),
                Role.CLIENT,
                pageable,
                ZoneId.of("Europe/Warsaw")
        );
        Page<ChatRoomDTO> caretakerChatRooms = chatService.getChatRoomsByCriteriaSortedByLastMessage(
                caretaker.getEmail(),
                Role.CARETAKER,
                pageable,
                ZoneId.of("Europe/Warsaw")
        );

        ChatRoomDTO clientChatRoom = clientChatRooms.getContent().get(0);
        ChatRoomDTO caretakerChatRoom = caretakerChatRooms.getContent().get(0);

        assertEquals(1, clientChatRooms.getContent().size());
        assertEquals(caretaker.getEmail(), clientChatRoom.getChatter().email());
        assertEquals(client.getEmail(), caretakerChatRoom.getChatter().email());

        assertEquals(caretaker.getAccountData().getName(), clientChatRoom.getChatter().name());
        assertEquals(client.getAccountData().getName(), caretakerChatRoom.getChatter().name());

        assertEquals(caretaker.getAccountData().getSurname(), clientChatRoom.getChatter().surname());
        assertEquals(client.getAccountData().getSurname(), caretakerChatRoom.getChatter().surname());

        assertNotNull(clientChatRoom.getLastMessage().getCreatedAt());
        assertNotNull(caretakerChatRoom.getLastMessage().getCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("provideTimeZones")
    void testGetChatRoomsByParticipantEmailWithZone_shouldSucceed(String timeZone) {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ChatRoomDTO> chatRooms = chatService.getChatRoomsByCriteriaSortedByLastMessage(
                client.getEmail(),
                Role.CLIENT,
                pageable,
                ZoneId.of(timeZone)
        );

        for(ChatRoomDTO chatRoomDTO : chatRooms.getContent()) {
            assertEquals(ZoneId.of(timeZone), chatRoomDTO.getLastMessage().getCreatedAt().getZone());
        }
    }

    @Test
    void testCreateMessage_shouldSucceed_asCaretaker() {
        long msgCount = chatMessageRepository.count();

        ChatMessageDTO msg = chatService.createMessage(
                chatRoom,
                client.getEmail(),
                Role.CLIENT,
                new ChatMessageSent("message content"),
                false
        );

        assertEquals(msgCount + 1, chatMessageRepository.count());
        assertEquals("message content", msg.getContent());
        assertEquals(client.getEmail(), msg.getSenderEmail());
        assertEquals(chatRoom.getId(), msg.getChatId());
        assertFalse(msg.getSeenByRecipient());
    }

    @Test
    void testCreateMessage_shouldSucceed_asClient() {
        long msgCount = chatMessageRepository.count();

        ChatMessageDTO msg = chatService.createMessage(
                chatRoom,
                caretaker.getEmail(),
                Role.CARETAKER,
                new ChatMessageSent("message content"),
                false
        );

        assertEquals(msgCount + 1, chatMessageRepository.count());
        assertEquals("message content", msg.getContent());
        assertEquals(caretaker.getEmail(), msg.getSenderEmail());
        assertEquals(chatRoom.getId(), msg.getChatId());
        assertFalse(msg.getSeenByRecipient());
    }

    @Test
    void testCreateMessage_shouldUpdateSeenByRecipientOnCreateNewMessage() {
        ChatMessageSent payload = new ChatMessageSent("message content");

        chatService.createMessage(
                chatRoom,
                caretaker.getEmail(),
                Role.CARETAKER,
                payload,
                false
        );

        chatService.createMessage(
                chatRoom,
                client.getEmail(),
                Role.CLIENT,
                payload,
                false
        );

        chatService.createMessage(
                chatRoom,
                caretaker.getEmail(),
                Role.CARETAKER,
                payload,
                false
        );

        List<ChatMessageDTO> clientMessages = chatService.getChatMessagesByParticipantEmail(
                chatRoom.getId(),
                client.getEmail(),
                PageRequest.of(0, 10),
                ZoneId.of("Europe/Warsaw")
        ).getContent();

        for(ChatMessageDTO chatMessageDTO : clientMessages.subList(1, clientMessages.size()-1)) {
            assertTrue(chatMessageDTO.getSeenByRecipient());
        }

        assertFalse(clientMessages.get(0).getSeenByRecipient());
    }

    @Test
    void testCreateMessage_chatRoomDoesNotExists_shouldThrowNotParticipateException() {
        ChatRoom chatRoom = ChatRoom.builder()
                .id(-1L)
                .client(Client.builder().email("otherClient").build())
                .caretaker(Caretaker.builder().email("otherCaretaker").build())
                .build();

        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom,
                        caretaker.getEmail(),
                        Role.CARETAKER,
                        new ChatMessageSent("message content"),
                        false
                )
        );
    }

    @Test
    void testCreateMessage_userDoesNotParticipateChat_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom,
                        "notAParticipant",
                        Role.CARETAKER,
                        new ChatMessageSent("message content"),
                        false
                )
        );
    }

    @Test
    void testCreateMessage_userParticipatesButAsRoleOtherThanProvided_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom,
                        client.getEmail(),
                        Role.CARETAKER,
                        new ChatMessageSent("message content"),
                        false
                )
        );

        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom,
                        caretaker.getEmail(),
                        Role.CLIENT,
                        new ChatMessageSent("message content"),
                        false
                )
        );
    }

    @Test
    void testCreateChatRoomWithMessage_shouldSucceed_asClient() {
        long chatsCount = chatRepository.count();

        ChatMessageDTO msg = chatService.createChatRoomWithMessage(
                otherClientWithCaretakerAccount.getEmail(),
                Role.CLIENT,
                otherCaretaker.getEmail(),
                new ChatMessageSent("message content"),
                ZoneId.of("Europe/Warsaw")
        );

        assertEquals("message content", msg.getContent());
        assertEquals(otherClientWithCaretakerAccount.getEmail(), msg.getSenderEmail());
        assertEquals(chatsCount + 1, chatRepository.count());
    }

    @Test
    void testCreateChatRoomWithMessage_shouldSucceed_asCaretaker() {
        long chatsCount = chatRepository.count();

        ChatMessageDTO msg = chatService.createChatRoomWithMessage(
                otherCaretaker.getEmail(),
                Role.CARETAKER,
                otherClientWithCaretakerAccount.getEmail(),
                new ChatMessageSent("message content"),
                ZoneId.of("Europe/Warsaw")
        );

        assertEquals("message content", msg.getContent());
        assertEquals(otherCaretaker.getEmail(), msg.getSenderEmail());
        assertEquals(chatsCount + 1, chatRepository.count());
    }

    @ParameterizedTest
    @MethodSource("provideTimeZones")
    void testCreateChatRoomWithMessage_timeZoneProvided_shouldSucceed(String timeZone) {
        ChatMessageDTO msg = chatService.createChatRoomWithMessage(
                otherClientWithCaretakerAccount.getEmail(),
                Role.CLIENT,
                otherCaretaker.getEmail(),
                new ChatMessageSent("message content"),
                ZoneId.of(timeZone)
        );

        assertEquals(ZoneId.of(timeZone), msg.getCreatedAt().getZone());
    }

    @Test
    void testCreateChatRoomWithMessage_sendMessageToYourself_shouldThrowInvalidMessageReceiverException() {
        assertThrows(
                InvalidMessageReceiverException.class,
                () -> chatService.createChatRoomWithMessage(
                        otherClientWithCaretakerAccount.getEmail(),
                        Role.CARETAKER,
                        otherClientWithCaretakerAccount.getEmail(),
                        new ChatMessageSent("message content"),
                        ZoneId.of("Europe/Warsaw")
                )
        );
    }

    @Test
    void testCreateChatRoomWithMessage_chatAlreadyExists_shouldThrowChatAlreadyExists() {
        assertThrows(
                ChatAlreadyExistsException.class,
                () -> chatService.createChatRoomWithMessage(
                        client.getEmail(),
                        Role.CLIENT,
                        caretaker.getEmail(),
                        new ChatMessageSent("message content"),
                        ZoneId.of("Europe/Warsaw")
                )
        );

        assertThrows(
                ChatAlreadyExistsException.class,
                () -> chatService.createChatRoomWithMessage(
                        caretaker.getEmail(),
                        Role.CARETAKER,
                        client.getEmail(),
                        new ChatMessageSent("message content"),
                        ZoneId.of("Europe/Warsaw")
                )
        );
    }

    @Test
    void testMarkMessagesAsSeen_clientUpdatesLastMessageSeen_shouldSucceed() {
        chatMessageRepository.save(
                createMockChatMessage(client.getAccountData(),  ZonedDateTime.now().plusYears(1), chatRoom)
        );

        chatMessageRepository.save(
                createMockChatMessage(caretaker.getAccountData(), ZonedDateTime.now(), chatRoom)
        );

        chatService.markMessagesAsSeen(chatRoom.getId(), client.getEmail());
        List<ChatMessageDTO> chatMessages = chatService.getChatMessagesByParticipantEmail(
                chatRoom.getId(),
                client.getEmail(),
                PageRequest.of(0, 10),
                ZoneId.of("Europe/Warsaw")
        ).getContent();

        for(ChatMessageDTO chatMessage : chatMessages) {
            if(!chatMessage.getSenderEmail().equals(client.getEmail())) {
                assertTrue(chatMessage.getSeenByRecipient());
            }
        }

        // Last message should not be marked as seen
        assertFalse(chatMessages.get(0).getSeenByRecipient());
    }

    @Test
    void testMarkMessagesAsSeen_caretakerUpdatesLastMessageSeen_shouldSucceed() {
        chatMessageRepository.save(
                createMockChatMessage(caretaker.getAccountData(), ZonedDateTime.now().plusYears(1), chatRoom)
        );

        chatMessageRepository.save(
                createMockChatMessage(client.getAccountData(), ZonedDateTime.now(), chatRoom)
        );

        chatService.markMessagesAsSeen(chatRoom.getId(), caretaker.getEmail());

        List<ChatMessageDTO> chatMessages = chatService.getChatMessagesByParticipantEmail(
                chatRoom.getId(),
                caretaker.getEmail(),
                PageRequest.of(0, 10),
                ZoneId.of("Europe/Warsaw")
        ).getContent();

        for(ChatMessageDTO chatMessage : chatMessages) {
            if(!chatMessage.getSenderEmail().equals(caretaker.getEmail())) {
                assertTrue(chatMessage.getSeenByRecipient());
            }
        }

        // Last message should not be marked as seen
        assertFalse(chatMessages.get(0).getSeenByRecipient());
    }

    @Test
    void testMarkMessagesAsSeen_userNotInChatRoom_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.markMessagesAsSeen(chatRoom.getId(), "notInChatRoom")
        );
    }

    @Test
    void testMarkMessagesAsSeen_noChatRoomWithProvidedId_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> chatService.markMessagesAsSeen(-1L, client.getEmail())
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidUserAndParticipant")
    void getChatRoomWithParticipant_shouldSucceed(
            String username,
            Role role,
            String participantUsername
    ) {
        ChatRoomDTO chatRoomDTO = chatService.getChatRoomWithParticipant(username, role, participantUsername, ZoneId.systemDefault());
        assertEquals(chatRoom.getId(), chatRoomDTO.getId());
        assertEquals(participantUsername, chatRoomDTO.getChatter().email());
        assertNotNull(chatRoomDTO.getLastMessage().getCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUserAndParticipant")
    void getChatRoomWithParticipant_userIsNotParticipant_shouldThrowNotFoundException(
            String username,
            Role role,
            String participantUsername
    ) {
        assertThrows(
                NotFoundException.class,
                () -> chatService.getChatRoomWithParticipant(username, role, participantUsername, ZoneId.systemDefault())
        );
    }

    @Test
    void markMessagesAsSeen_shouldSucceed() {
        chatService.createMessage(
                chatRoom,
                caretaker.getEmail(),
                Role.CARETAKER,
                new ChatMessageSent("message content"),
                false
        );

        chatService.markMessagesAsSeen(chatRoom.getId(), client.getEmail());

        List<ChatMessageDTO> chatMessages = chatService.getChatMessagesByParticipantEmail(
                chatRoom.getId(),
                client.getEmail(),
                PageRequest.of(0, 10),
                ZoneId.of("Europe/Warsaw")
        ).getContent();

        for(ChatMessageDTO chatMessage : chatMessages) {
            if(!chatMessage.getSenderEmail().equals(client.getEmail())) {
                assertTrue(chatMessage.getSeenByRecipient());
            }
        }
    }

    @Test
    void getUnreadChatsNumber_shouldReturnProperAnswer() {
        UnseenChatsNotificationDTO unreadChatsNumber = chatService.getUnseenChatsNumberNotification(client.getEmail());
        assertEquals(1, unreadChatsNumber.getUnseenChatsAsClient());
        assertEquals(0, unreadChatsNumber.getUnseenChatsAsCaretaker());
    }

    @Test
    void getUnreadChatsNumber_WhenUserHasChatsAsClientAndAsCaretaker_shouldReturnProperAnswer() {

        transactionTemplate.execute(status -> {
            ChatMessage seenMessage = createMockChatMessage(caretaker.getAccountData(), ZonedDateTime.now());
            seenMessage.setSeenByRecipient(true);

            ChatRoom seenChatRoom = PersistenceUtils.addChatRoom(
                    createMockChatRoom(otherClientWithCaretakerAccount, caretaker),
                    List.of(seenMessage),
                    chatRepository,
                    chatMessageRepository
            );

            ChatRoom unSeenChatRoom = PersistenceUtils.addChatRoom(
                    createMockChatRoom(client, otherCaretakerWithClientAccount),
                    createMockChatMessages(client, otherCaretakerWithClientAccount),
                    chatRepository,
                    chatMessageRepository
            );
            return null;
        });

        UnseenChatsNotificationDTO unreadChatsNumber = chatService.getUnseenChatsNumberNotification(otherClientWithCaretakerAccount.getEmail());
        assertEquals(0, unreadChatsNumber.getUnseenChatsAsClient());
        assertEquals(1, unreadChatsNumber.getUnseenChatsAsCaretaker());
    }

    private static Stream<String> provideTimeZones() {
        return Stream.of(
            "UTC",
            "Europe/Warsaw",
            "Asia/Tokyo"
        );
    }

    private static Stream<Arguments> provideValidUserAndParticipant() {
        return Stream.of(
                Arguments.of(client.getEmail(), Role.CLIENT, caretaker.getEmail()),
                Arguments.of(caretaker.getEmail(), Role.CARETAKER, client.getEmail())
        );
    }

    private static Stream<Arguments> provideInvalidUserAndParticipant() {
        return Stream.of(
                Arguments.of(client.getEmail(), Role.CARETAKER, caretaker.getEmail()),
                Arguments.of(caretaker.getEmail(), Role.CLIENT, client.getEmail())
        );
    }
}

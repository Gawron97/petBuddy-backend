package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
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
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.chat.NotParticipateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.ZoneId;
import java.util.stream.Stream;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ChatServiceTest {

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

    private ChatRoom chatRoom;
    private Client client;
    private Client otherClientWithCaretakerAccount;
    private Caretaker caretaker;
    private Caretaker otherCaretaker;

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
                MockChatProvider.createMockChatRoom(client, caretaker),
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

        PersistenceUtils.addCaretaker(
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

        Page<ChatMessageDTO> clientChatMessages = chatService.getChatMessages(
                chatRoom.getId(),
                client.getEmail(),
                pageable,
                ZoneId.of("Europe/Warsaw")
        );
        Page<ChatMessageDTO> caretakerChatMessages = chatService.getChatMessages(
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

        Page<ChatMessageDTO> clientChatMessages = chatService.getChatMessages(chatRoom.getId(), client.getEmail(), pageable, zoneId);

        for(ChatMessageDTO chatMessageDTO : clientChatMessages.getContent()) {
            assertEquals(zoneId, chatMessageDTO.getCreatedAt().getZone());
        }
    }

    @Test
    void testGetChatMessages_chatDoesNotExist_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> chatService.getChatMessages(-1L, "", null, ZoneId.of("Europe/Warsaw"))
        );
    }

    @Test
    void testGetChatMessages_userDoesNotParticipateChat_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.getChatMessages(chatRoom.getId(), "notAParticipant", null, ZoneId.of("Europe/Warsaw"))
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

        Page<ChatRoomDTO> clientChatRooms = chatService.getChatRoomsByParticipantEmail(
                client.getEmail(),
                Role.CLIENT,
                pageable,
                ZoneId.of("Europe/Warsaw")
        );
        Page<ChatRoomDTO> caretakerChatRooms = chatService.getChatRoomsByParticipantEmail(
                caretaker.getEmail(),
                Role.CARETAKER,
                pageable,
                ZoneId.of("Europe/Warsaw")
        );

        ChatRoomDTO clientChatRoom = clientChatRooms.getContent().get(0);
        ChatRoomDTO caretakerChatRoom = caretakerChatRooms.getContent().get(0);

        assertEquals(1, clientChatRooms.getContent().size());
        assertEquals(caretaker.getEmail(), clientChatRoom.getChatterEmail());
        assertEquals(client.getEmail(), caretakerChatRoom.getChatterEmail());

        assertEquals(caretaker.getAccountData().getName(), clientChatRoom.getChatterName());
        assertEquals(client.getAccountData().getName(), caretakerChatRoom.getChatterName());

        assertEquals(caretaker.getAccountData().getSurname(), clientChatRoom.getChatterSurname());
        assertEquals(client.getAccountData().getSurname(), caretakerChatRoom.getChatterSurname());

        assertNotNull(clientChatRoom.getLastMessageCreatedAt());
        assertNotNull(caretakerChatRoom.getLastMessageCreatedAt());
    }

    @ParameterizedTest
    @MethodSource("provideTimeZones")
    void testGetChatRoomsByParticipantEmailWithZone_shouldSucceed(String timeZone) {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ChatRoomDTO> chatRooms = chatService.getChatRoomsByParticipantEmail(
                client.getEmail(),
                Role.CLIENT,
                pageable,
                ZoneId.of(timeZone)
        );

        for(ChatRoomDTO chatRoomDTO : chatRooms.getContent()) {
            assertEquals(ZoneId.of(timeZone), chatRoomDTO.getLastMessageCreatedAt().getZone());
        }
    }

    @Test
    void testCreateMessage_shouldSucceed_asCaretaker() {
        long msgCount = chatMessageRepository.count();

        ChatMessageDTO msg = chatService.createMessage(
                chatRoom.getId(),
                client.getEmail(),
                new ChatMessageSent("message content"),
                Role.CLIENT
        );

        assertEquals(msgCount + 1, chatMessageRepository.count());
        assertEquals("message content", msg.getContent());
        assertEquals(client.getEmail(), msg.getSenderEmail());
        assertEquals(chatRoom.getId(), msg.getChatId());
    }

    @Test
    void testCreateMessage_shouldSucceed_asClient() {
        long msgCount = chatMessageRepository.count();

        ChatMessageDTO msg = chatService.createMessage(
                chatRoom.getId(),
                caretaker.getEmail(),
                new ChatMessageSent("message content"),
                Role.CARETAKER
        );

        assertEquals(msgCount + 1, chatMessageRepository.count());
        assertEquals("message content", msg.getContent());
        assertEquals(caretaker.getEmail(), msg.getSenderEmail());
        assertEquals(chatRoom.getId(), msg.getChatId());
    }

    @Test
    void testCreateMessage_chatRoomDoesNotExists_shouldThrowNotFoundException() {
        assertThrows(
                NotFoundException.class,
                () -> chatService.createMessage(
                        -1L,
                        caretaker.getEmail(),
                        new ChatMessageSent("message content"),
                        Role.CARETAKER
                )
        );
    }

    @Test
    void testCreateMessage_userDoesNotParticipateChat_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom.getId(),
                        "notAParticipant",
                        new ChatMessageSent("message content"),
                        Role.CARETAKER
                )
        );
    }

    @Test
    void testCreateMessage_userParticipatesButAsRoleOtherThanProvided_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom.getId(),
                        client.getEmail(),
                        new ChatMessageSent("message content"),
                        Role.CARETAKER
                )
        );

        assertThrows(
                NotParticipateException.class,
                () -> chatService.createMessage(
                        chatRoom.getId(),
                        caretaker.getEmail(),
                        new ChatMessageSent("message content"),
                        Role.CLIENT
                )
        );
    }

    @Test
    void testCreateChatRoomWithMessage_shouldSucceed_asClient() {
        long chatsCount = chatRepository.count();

        ChatMessageDTO msg = chatService.createChatRoomWithMessage(
            otherCaretaker.getEmail(),
            otherClientWithCaretakerAccount.getEmail(),
            Role.CLIENT,
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
                otherClientWithCaretakerAccount.getEmail(),
                otherCaretaker.getEmail(),
                Role.CARETAKER,
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
                otherCaretaker.getEmail(),
                otherClientWithCaretakerAccount.getEmail(),
                Role.CLIENT,
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
                        otherClientWithCaretakerAccount.getEmail(),
                        Role.CARETAKER,
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
                        caretaker.getEmail(),
                        client.getEmail(),
                        Role.CLIENT,
                        new ChatMessageSent("message content"),
                        ZoneId.of("Europe/Warsaw")
                )
        );

        assertThrows(
                ChatAlreadyExistsException.class,
                () -> chatService.createChatRoomWithMessage(
                        client.getEmail(),
                        caretaker.getEmail(),
                        Role.CARETAKER,
                        new ChatMessageSent("message content"),
                        ZoneId.of("Europe/Warsaw")
                )
        );
    }

    private static Stream<String> provideTimeZones() {
        return Stream.of(
            "UTC",
            "Europe/Warsaw",
            "Asia/Tokyo"
        );
    }
}

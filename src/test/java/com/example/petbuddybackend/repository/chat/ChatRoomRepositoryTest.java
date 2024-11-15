package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.testutils.ValidationUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.petbuddybackend.testutils.mock.MockChatProvider.*;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ChatRoom chatRoomSameCreatedAtFst;
    private ChatRoom chatRoomSameCreatedAtSnd;
    private ChatRoom chatRoomDifferentCreatedAt;

    @BeforeEach
    void setUp() {
        ZonedDateTime sameDateTime = ZonedDateTime.now().minusDays(1);
        chatRoomDifferentCreatedAt = PersistenceUtils.createChatRoomWithMessages(
                appUserRepository,
                clientRepository,
                caretakerRepository,
                chatRoomRepository,
                chatMessageRepository,
                "clientDifferentCreatedAt",
                "caretakerDifferentCreatedAt"
        );
        chatRoomSameCreatedAtFst = PersistenceUtils.createChatRoomWithMessages(
                appUserRepository,
                clientRepository,
                caretakerRepository,
                chatRoomRepository,
                chatMessageRepository,
                "clientSameCreatedAtFst",
                "caretakerSameCreatedAtFst",
                sameDateTime
        );
        chatRoomSameCreatedAtSnd = PersistenceUtils.createChatRoomWithMessages(
                appUserRepository,
                clientRepository,
                caretakerRepository,
                chatRoomRepository,
                chatMessageRepository,
                "clientSameCreatedAtSnd",
                "caretakerSameCreatedAtSnd",
                sameDateTime
        );
    }

    @AfterEach
    void tearDown() {
        chatRepository.deleteAll();
        clientRepository.deleteAll();
        caretakerRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testFindByCaretakerEmail_messagesWithDifferentCreatedAT_shouldReturnChatRoomDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(chatRoomDifferentCreatedAt.getCaretaker().getEmail(), pageable);

        // Chat rooms have different created at messages so the latest message should be the latest created at message
        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoomDifferentCreatedAt.getMessages().stream()
                .max(Comparator.comparing(ChatMessage::getCreatedAt))
                .get();

        assertTrue(allMessagesHaveDifferentCreatedAt(chatRoomDifferentCreatedAt.getMessages()));
        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    @Test
    @Transactional
    void testFindByClientEmail_messagesWithDifferentCreatedAT_shouldReturnChatRoomDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByClientEmailSortByLastMessageDesc(chatRoomDifferentCreatedAt.getClient().getEmail(), pageable);

        // Chat rooms have different created at messages so the latest message should be the latest created at message
        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoomDifferentCreatedAt.getMessages().stream()
                .max(Comparator.comparing(ChatMessage::getCreatedAt))
                .get();

        assertTrue(allMessagesHaveDifferentCreatedAt(chatRoomDifferentCreatedAt.getMessages()));
        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    @Test
    void testFindByCaretakerEmail_messagesWithSameCreatedAT_shouldReturnChatRoomDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(chatRoomSameCreatedAtFst.getCaretaker().getEmail(), pageable);

        // Chat rooms have same created at messages so the latest message should be the message with the lowest id
        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoomSameCreatedAtFst.getMessages().stream()
                .min(Comparator.comparing(ChatMessage::getId))
                .get();

        assertTrue(allMessagesHaveSameCreatedAt(chatRoomSameCreatedAtFst.getMessages()));
        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    @Test
    void testFindByClientEmail_messagesWithSameCreatedAT_shouldReturnChatRoomDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByClientEmailSortByLastMessageDesc(chatRoomSameCreatedAtFst.getClient().getEmail(), pageable);

        // Chat rooms have same created at messages so the latest message should be the message with the lowest id
        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoomSameCreatedAtFst.getMessages().stream()
                .min(Comparator.comparing(ChatMessage::getId))
                .get();

        assertTrue(allMessagesHaveSameCreatedAt(chatRoomSameCreatedAtFst.getMessages()));
        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    @Test
    void testFindByCaretakerEmail_differentChatRoomsWithSameMessageCreatedAt_shouldReturnCorrectChatRoomWithMessage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(chatRoomSameCreatedAtSnd.getCaretaker().getEmail(), pageable);

        assertEquals(1, chatRooms.getTotalElements());
        assertEquals(chatRoomSameCreatedAtSnd.getClient().getEmail(), chatRooms.getContent().get(0).getLastMessageSendBy());
    }

    @Test
    void testFindByClientEmail_differentChatRoomsWithSameMessageCreatedAt_shouldReturnCorrectChatRoomWithMessage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByClientEmailSortByLastMessageDesc(chatRoomSameCreatedAtSnd.getClient().getEmail(), pageable);

        assertEquals(1, chatRooms.getTotalElements());
        assertEquals(chatRoomSameCreatedAtSnd.getClient().getEmail(), chatRooms.getContent().get(0).getLastMessageSendBy());
    }


    @Test
    void testCountUnreadChatsForUser_whenMessagesAtDifferentTime_shouldReturnCorrectCount() {
        long unreadChats = chatRepository.countUnreadChatsForUserAsClient(chatRoomDifferentCreatedAt.getClient().getEmail());
        assertEquals(1, unreadChats);
    }

    @Test
    void testCountUnreadChatsForUser_whenMessagesAtSameTime_shouldReturnCorrectCount() {
        long unreadChats = chatRepository.countUnreadChatsForUserAsClient(chatRoomSameCreatedAtFst.getClient().getEmail());
        assertEquals(1, unreadChats);
    }

    @Test
    void testCountUnreadChatsForUser_whenUserHasMultipleUnreadChats_shouldReturnCorrectCount() {

        //Given
        transactionTemplate.execute(status ->
            PersistenceUtils.createChatRoomWithMessages(
                appUserRepository,
                clientRepository,
                caretakerRepository,
                chatRoomRepository,
                chatMessageRepository,
                chatRoomDifferentCreatedAt.getClient().getEmail(),
                "differentCaretaker"
            )
        );

        long unreadChats = chatRepository.countUnreadChatsForUserAsClient(chatRoomDifferentCreatedAt.getClient().getEmail());
        assertEquals(2, unreadChats);
    }

    @Test
    void testCountUnreadChatsForUser_whenUserHasUnreadChatAsClientButNotAsCaretaker_shouldReturnCorrectCount() {

        //Given
        String User1Email = transactionTemplate.execute(status -> {

            Caretaker caretakerUser1 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository, createMockCaretaker("user1"));
            Client clientUser1 = clientRepository.save(
                    Client.builder()
                            .email(caretakerUser1.getEmail())
                            .accountData(caretakerUser1.getAccountData())
                            .build()
            );

            Client clientUser2 = PersistenceUtils.addClient(appUserRepository, clientRepository, createMockClient("user2"));
            Caretaker caretakerUser2 = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository, createMockCaretaker("user2"));

            ChatRoom unSeenChatRoom = PersistenceUtils.addChatRoom(
                    createMockChatRoom(clientUser1, caretakerUser2),
                    createMockChatMessages(clientUser1, caretakerUser2),
                    chatRepository,
                    chatMessageRepository
            );

            ChatMessage seenMessage = createMockChatMessage(clientUser2.getAccountData(), ZonedDateTime.now());
            seenMessage.setSeenByRecipient(true);

            ChatRoom seenChatRoom = PersistenceUtils.addChatRoom(
                    createMockChatRoom(clientUser2, caretakerUser1),
                    List.of(seenMessage),
                    chatRepository,
                    chatMessageRepository
            );
            return "user1";

        });
        long unreadChatsAsClient = chatRepository.countUnreadChatsForUserAsClient(User1Email);
        long unreadChatsAsCaretaker = chatRepository.countUnreadChatsForUserAsCaretaker(User1Email);
        assertEquals(1, unreadChatsAsClient);
        assertEquals(0, unreadChatsAsCaretaker);
    }

    @Test
    void testCountUnreadChatsForUser_whenOnlyUserSendMessagesToChat_ShouldReturnZeroUnreadChats() {

        //Given
        transactionTemplate.execute(status -> {
                Client client = PersistenceUtils.addClient(appUserRepository, clientRepository, createMockClient("client"));
                Caretaker caretaker = PersistenceUtils.addCaretaker(caretakerRepository, appUserRepository, createMockCaretaker("caretaker"));
                PersistenceUtils.addChatRoom(
                        createMockChatRoom(client, caretaker),
                        List.of(createMockChatMessage(client.getAccountData())),
                        chatRoomRepository,
                        chatMessageRepository
                );
                return null;
            }
        );

        //When
        int countUnreadChats = chatRepository.countUnreadChatsForUserAsClient("client");

        //Then
        assertEquals(0, countUnreadChats);

    }

    private boolean allMessagesHaveSameCreatedAt(List<ChatMessage> messages) {
        return messages.stream()
                .map(ChatMessage::getCreatedAt)
                .collect(Collectors.toSet())
                .size() == 1;
    }

    private boolean allMessagesHaveDifferentCreatedAt(List<ChatMessage> messages) {
        return messages.stream()
                .map(ChatMessage::getCreatedAt)
                .collect(Collectors.toSet())
                .size() == messages.size();
    }
}

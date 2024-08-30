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
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    private ChatRoom chatRoomSameCreatedAt;
    private ChatRoom chatRoomDifferentCreatedAt;

    @BeforeEach
    void setUp() {
        chatRoomSameCreatedAt = createChatRoomWithSameCreatedAtMessages();
        chatRoomDifferentCreatedAt = createChatRoomWithDifferentCreatedAtMessages();
    }

    @AfterEach
    void tearDown() {
        chatRepository.deleteAll();
        clientRepository.deleteAll();
        caretakerRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void testFindByCaretakerEmailSortByLastMessageDesc_messagesWithDifferentCreatedAT_shouldReturnChatRoomDTOs() {
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
    void testFindByClientEmailSortByLastMessageDesc_messagesWithDifferentCreatedAT_shouldReturnChatRoomDTOs() {
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
    void testFindByCaretakerEmailSortByLastMessageDesc_messagesWithSameCreatedAT_shouldReturnChatRoomDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(chatRoomSameCreatedAt.getCaretaker().getEmail(), pageable);

        // Chat rooms have same created at messages so the latest message should be the message with the lowest id
        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoomSameCreatedAt.getMessages().stream()
                .min(Comparator.comparing(ChatMessage::getId))
                .get();

        assertTrue(allMessagesHaveSameCreatedAt(chatRoomSameCreatedAt.getMessages()));
        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    @Test
    @Transactional
    void testFindByClientEmailSortByLastMessageDesc_messagesWithSameCreatedAT_shouldReturnChatRoomDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByClientEmailSortByLastMessageDesc(chatRoomSameCreatedAt.getClient().getEmail(), pageable);

        // Chat rooms have same created at messages so the latest message should be the message with the lowest id
        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoomSameCreatedAt.getMessages().stream()
                .min(Comparator.comparing(ChatMessage::getId))
                .get();

        assertTrue(allMessagesHaveSameCreatedAt(chatRoomSameCreatedAt.getMessages()));
        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    private ChatRoom createChatRoomWithSameCreatedAtMessages() {
        Caretaker caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker("caretakerSameCreatedAt")
        );

        Client client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient("clientSameCreatedAt")
        );

        ZonedDateTime createdAt = ZonedDateTime.now();

        List<ChatMessage> messages = List.of(
                MockChatProvider.createMockChatMessage(client.getAccountData(), createdAt),
                MockChatProvider.createMockChatMessage(caretaker.getAccountData(), createdAt)
        );

        return PersistenceUtils.addChatRoom(
                MockChatProvider.createMockChatRoom(client, caretaker),
                messages,
                chatRepository,
                chatMessageRepository
        );
    }

    private ChatRoom createChatRoomWithDifferentCreatedAtMessages() {
        Caretaker caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker("caretakerDifferentCreatedAt")
        );

        Client client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient("clientDifferentCreatedAt")
        );

        List<ChatMessage> messages = List.of(
                MockChatProvider.createMockChatMessage(client.getAccountData(), ZonedDateTime.now()),
                MockChatProvider.createMockChatMessage(caretaker.getAccountData(), ZonedDateTime.now().minusDays(1))
        );

        return PersistenceUtils.addChatRoom(
                MockChatProvider.createMockChatRoom(client, caretaker),
                messages,
                chatRepository,
                chatMessageRepository
        );
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

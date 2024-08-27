package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.MockUtils;
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

import java.util.Comparator;

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

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        Caretaker caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUtils.createMockCaretaker()
        );

        Client client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUtils.createMockClient()
        );

        chatRoom = PersistenceUtils.addChatRoom(
                MockUtils.createMockChatRoom(client, caretaker),
                MockUtils.createMockChatMessages(client, caretaker),
                chatRepository,
                chatMessageRepository
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
    void testFindByCaretakerEmailSortByLastMessageDesc_shouldReturnChatRoomDTOs() throws IllegalAccessException {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByCaretakerEmailSortByLastMessageDesc(chatRoom.getCaretaker().getEmail(), pageable);

        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoom.getMessages().stream()
                .max(Comparator.comparing(ChatMessage::getCreatedAt))
                .get();

        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }

    @Test
    @Transactional
    void testFindByClientEmailSortByLastMessageDesc_shouldReturnChatRoomDTOs() throws IllegalAccessException {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatRoomDTO> chatRooms =
                chatRepository.findByClientEmailSortByLastMessageDesc(chatRoom.getClient().getEmail(), pageable);

        ChatRoomDTO returnedChatRoom = chatRooms.getContent().get(0);
        ChatMessage messageThatShouldBeLatest = chatRoom.getMessages().stream()
                .max(Comparator.comparing(ChatMessage::getCreatedAt))
                .get();

        assertEquals(1, chatRooms.getTotalElements());
        assertTrue(ValidationUtils.fieldsNotNullRecursive(chatRooms.getContent().get(0)));
        assertEquals(messageThatShouldBeLatest.getContent(), returnedChatRoom.getLastMessage());
    }
}

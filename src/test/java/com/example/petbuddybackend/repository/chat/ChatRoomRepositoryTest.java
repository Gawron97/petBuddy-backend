package com.example.petbuddybackend.repository.chat;

import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
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

    private ChatRoom chatRoomSameCreatedAtFst;
    private ChatRoom chatRoomSameCreatedAtSnd;
    private ChatRoom chatRoomDifferentCreatedAt;
    @Autowired
    private ChatRoomRepository chatRoomRepository;

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

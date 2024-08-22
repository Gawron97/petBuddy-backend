package com.example.petbuddybackend.service.chat;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.MockUtils;
import com.example.petbuddybackend.testutils.PersistenceUtils;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.NotParticipateException;
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
    private Caretaker caretaker;

    @BeforeEach
    void setUp() {
        caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUtils.createMockCaretaker()
        );

        client = PersistenceUtils.addClient(
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
        appUserRepository.deleteAll();
    }

    @Test
    void testGetChatMessages_shouldSucceed() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ChatMessageDTO> clientChatMessages = chatService.getChatMessages(chatRoom.getId(), client.getEmail(), pageable);
        Page<ChatMessageDTO> caretakerChatMessages = chatService.getChatMessages(chatRoom.getId(), caretaker.getEmail(), pageable);

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
                () -> chatService.getChatMessages(-1L, "", null)
        );
    }

    @Test
    void testGetChatMessages_userDoesNotParticipateChat_shouldThrowNotParticipateException() {
        assertThrows(
                NotParticipateException.class,
                () -> chatService.getChatMessages(chatRoom.getId(), "notAParticipant", null)
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

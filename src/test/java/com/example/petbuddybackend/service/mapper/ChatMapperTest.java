package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ChatMapperTest {

    private final ChatMapper mapper = ChatMapper.INSTANCE;

    private ChatMessage chatMessage;
    private ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
    private ZoneId warsawZone = ZoneId.of("Europe/Warsaw");

    @BeforeEach
    void setUp() {
        chatMessage = MockChatProvider.createMockChatMessage(MockUserProvider.createMockAppUser());
        Client client = MockUserProvider.createMockClient();
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        ChatRoom chatRoom = MockChatProvider.createMockChatRoom(client, caretaker);
        chatRoom.setId(1L);
        chatMessage.setId(2L);
        chatMessage.setSeenByRecipient(true);
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setCreatedAt(ZonedDateTime.now().withZoneSameInstant(tokyoZone));
    }

    @Test
    void mapToChatMessageDTO_includesTimeZone_shouldNotLeaveNullFields() {
        ChatMessageDTO mappedChatMessage = mapper.mapToChatMessageDTO(chatMessage, warsawZone);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(mappedChatMessage));
        assertEquals(tokyoZone, chatMessage.getCreatedAt().getZone());
        assertEquals(warsawZone, mappedChatMessage.getCreatedAt().getZone());
    }

    @Test
    void mapToChatMessageDTO_shouldNotLeaveNullFields() {
        ChatMessageDTO mappedChatMessage = mapper.mapToChatMessageDTO(chatMessage);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(mappedChatMessage));
    }

    @Test
    void mapTimeZoneFromChatMessageDTO_shouldChangeTimeZone() {
        ChatMessageDTO messageDTO = ChatMessageDTO.builder()
                .createdAt(ZonedDateTime.now().withZoneSameInstant(tokyoZone))
                .build();

        ChatMessageDTO mappedChatMessageWithNewZone = mapper.mapTimeZone(messageDTO, warsawZone);
        ZoneId newZone = mappedChatMessageWithNewZone.getCreatedAt().getZone();

        assertEquals(warsawZone, newZone);
    }

    @Test
    void mapTimeZoneFromChatRoomDTO_shouldChangeTimeZone() {
        ChatRoomDTO chatRoomDTO = ChatRoomDTO.builder()
                .lastMessage(
                        ChatMessageDTO.builder()
                                .createdAt(ZonedDateTime.now().withZoneSameInstant(tokyoZone))
                                .build())
                .build();

        ChatRoomDTO mappedChatMessageWithNewZone = mapper.mapTimeZone(chatRoomDTO, warsawZone);
        ZoneId newZone = mappedChatMessageWithNewZone.getLastMessage().getCreatedAt().getZone();

        assertEquals(warsawZone, newZone);
    }

    @Test
    void mapToChatRoomDTO_shouldNotLeaveNullFields() {
        ChatRoomDTO mappedChatRoom = mapper.mapToChatRoomDTO(
                1L,
                MockUserProvider.createMockAppUserWithPhoto(),
                chatMessage,
                ZoneId.systemDefault()
        );
        assertTrue(ValidationUtils.fieldsNotNullRecursive(mappedChatRoom));
    }
}

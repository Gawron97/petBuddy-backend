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

    @BeforeEach
    void setUp() {
        chatMessage = MockChatProvider.createMockChatMessage(MockUserProvider.createMockAppUser());
        Client client = MockUserProvider.createMockClient();
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        ChatRoom chatRoom = MockChatProvider.createMockChatRoom(client, caretaker);
        chatRoom.setId(1L);
        chatMessage.setId(2L);
        chatMessage.setChatRoom(chatRoom);
    }

    @Test
    void mapToChatMessageDTO_shouldNotLeaveNullFields() {
        ChatMessageDTO mappedChatMessage = mapper.mapToChatMessageDTO(chatMessage);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(mappedChatMessage));
    }

    @Test
    void mapTimeZoneFromChatMessageDTO_shouldChangeTimeZone() {
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        ZoneId warsawZone = ZoneId.of("Europe/Warsaw");

        ChatMessageDTO messageDTO = ChatMessageDTO.builder()
                .createdAt(ZonedDateTime.now().withZoneSameInstant(tokyoZone))
                .build();

        ChatMessageDTO mappedChatMessageWithNewZone = mapper.mapTimeZone(messageDTO, warsawZone);
        ZoneId newZone = mappedChatMessageWithNewZone.getCreatedAt().getZone();

        assertEquals(warsawZone, newZone);
    }

    @Test
    void mapTimeZoneFromChatRoomDTO_shouldChangeTimeZone() {
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        ZoneId warsawZone = ZoneId.of("Europe/Warsaw");

        ChatRoomDTO chatRoomDTO = ChatRoomDTO.builder()
                .lastMessageCreatedAt(ZonedDateTime.now().withZoneSameInstant(tokyoZone))
                .build();

        ChatRoomDTO mappedChatMessageWithNewZone = mapper.mapTimeZone(chatRoomDTO, warsawZone);
        ZoneId newZone = mappedChatMessageWithNewZone.getLastMessageCreatedAt().getZone();

        assertEquals(warsawZone, newZone);
    }
}

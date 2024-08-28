package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO make tests
public class ChatMapperTest {

    private final ChatMapper mapper = ChatMapper.INSTANCE;


    @Test
    void mapToChatMessageDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        ChatMessage chatMessage = MockChatProvider.createMockChatMessage(MockUserProvider.createMockAppUser());
        Client client = MockUserProvider.createMockClient();
        Caretaker caretaker = MockUserProvider.createMockCaretaker();
        ChatRoom chatRoom = MockChatProvider.createMockChatRoom(client, caretaker);
        chatRoom.setId(1L);
        chatMessage.setId(2L);
        chatMessage.setChatRoom(chatRoom);

        ChatMessageDTO mappedChatMessage = mapper.mapToChatMessageDTO(chatMessage);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(mappedChatMessage));
    }

    @Test
    void mapTimeZoneFromChatMessageDTO_shouldChangeTimeZone() {
    }
}

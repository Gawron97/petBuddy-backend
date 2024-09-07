package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockChatProvider {

    public static List<ChatMessage> createMockChatMessages(Client client, Caretaker caretaker) {
        AppUser clientAppUser = client.getAccountData();
        AppUser caretakerAppUser = caretaker.getAccountData();

        ChatMessage clientMessage = createMockChatMessage(clientAppUser);
        ChatMessage caretakerMessage = createMockChatMessage(caretakerAppUser);

        return List.of(clientMessage, caretakerMessage);
    }

    public static ChatMessage createMockChatMessage(AppUser sender, ZonedDateTime createdAt, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .content(UUID.randomUUID().toString())
                .createdAt(createdAt)
                .chatRoom(chatRoom)
                .sender(sender)
                .build();
    }

    public static ChatMessage createMockChatMessage(AppUser sender, ZonedDateTime createdAt) {
        return createMockChatMessage(sender, createdAt, null);
    }

    public static ChatMessage createMockChatMessage(AppUser sender) {
        return createMockChatMessage(sender, ZonedDateTime.now());
    }

    public static ChatRoom createMockChatRoom(Client client, Caretaker caretaker) {
        return ChatRoom.builder()
                .client(client)
                .caretaker(caretaker)
                .build();
    }

    public static ChatMessageDTO createMockChatMessageDTO() {
        return ChatMessageDTO.builder()
                .id(1L)
                .createdAt(ZonedDateTime.now())
                .senderEmail("email")
                .content("message content")
                .build();
    }
}

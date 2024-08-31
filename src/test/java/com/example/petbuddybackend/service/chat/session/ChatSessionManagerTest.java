package com.example.petbuddybackend.service.chat.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionManagerTest {

    private ChatSessionManager chatSessionManager;

    @BeforeEach
    void setUp() {
        chatSessionManager = new ChatSessionManager();
    }

    @Test
    void get_chatRoomDoesNotExist_shouldReturnNull() {
        Long chatId = 1L;

        assertThrows(IllegalArgumentException.class, () -> chatSessionManager.get(chatId));
    }

    @Test
    void get_userNotInChatRoom_shouldReturnNull() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        assertThrows(IllegalArgumentException.class, () -> chatSessionManager.get(chatId, "user1"));
    }

    @Test
    void get_userExists_shouldReturnUserMetadata() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        ChatUserMetadata returnedUserMetadata = chatSessionManager.get(chatId, "username");

        assertEquals(userMetadata, returnedUserMetadata);
    }

    @Test
    void get_chatRoomExists_shouldReturnChatRoomMetadata() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        ChatRoomMetadata metadata = chatSessionManager.get(chatId);

        assertEquals(1, metadata.size());
        assertEquals(userMetadata, metadata.get("username"));
    }

    @Test
    void computeIfAbsent_shouldCreateNewChatRoomWhenNotPresent() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        ChatRoomMetadata metadata = chatSessionManager.get(chatId);
        assertEquals(1, metadata.size());
        assertEquals(userMetadata, metadata.get("username"));
    }

    @Test
    void computeIfAbsent_userAlreadyExists_shouldSkip() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata("username", ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata("username", ZoneId.of("Europe/Warsaw"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata1);
        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata2);

        ChatRoomMetadata metadata = chatSessionManager.get(chatId);
        ChatUserMetadata returnedUserMetadata = metadata.get("username");

        assertEquals(1, metadata.size());
        assertEquals(userMetadata1, returnedUserMetadata);
        assertNotEquals(userMetadata2, returnedUserMetadata);
    }

    @Test
    void computeIfAbsent_userNotExistsInExistingChatRoom_shouldBeAdded() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata("username1", ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata("username2", ZoneId.of("Europe/Warsaw"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata1);
        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata2);

        ChatRoomMetadata metadata = chatSessionManager.get(chatId);

        assertEquals(2, metadata.size());
        assertEquals(userMetadata1, metadata.get("username1"));
        assertEquals(userMetadata2, metadata.get("username2"));
    }
    
    @Test
    void computeIfAbsent_chatRoomIsFull_shouldSkip() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata("username1", ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata("username2", ZoneId.of("Europe/Warsaw"));
        ChatUserMetadata userMetadata3 = new ChatUserMetadata("username3", ZoneId.of("Europe/Warsaw"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata1);
        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata2);
        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata3);

        ChatRoomMetadata metadata = chatSessionManager.get(chatId);

        assertEquals(2, metadata.size());
        assertEquals(userMetadata1, metadata.get("username1"));
        assertEquals(userMetadata2, metadata.get("username2"));
        assertThrows(IllegalArgumentException.class, () -> metadata.get("username3"));
    }

    @Test
    void removeIfPresent_chatRoomDoesNotExist_shouldReturnEmpty() {
        Long chatId = 1L;
        String username = "user1";

        Optional<ChatUserMetadata> result = chatSessionManager.removeIfPresent(chatId, username);

        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfPresent_userNotInChatRoom_shouldReturnEmpty() {
        Long chatId = 1L;
        String username = "username";
        ChatUserMetadata userMetadata = new ChatUserMetadata(username, ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfPresent(chatId, "user1");

        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfPresent_userNotInProvidedChatRoom_shouldReturnEmpty() {
        Long chatId = 1L;
        String username = "username";
        String otherUsername = "otherUsername";
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(username, ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata1);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfPresent(chatId, otherUsername);

        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfPresent_userExists_shouldReturnUserMetadata() {
        Long chatId = 1L;
        String username = "username";
        ChatUserMetadata userMetadata = new ChatUserMetadata(username, ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfPresent(chatId, username);

        assertTrue(result.isPresent());
    }
}

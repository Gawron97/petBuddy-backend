package com.example.petbuddybackend.service.chat.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        assertEquals(1, chatSessionManager.size());
        assertThrows(IllegalArgumentException.class, () -> chatSessionManager.get(chatId, "user1"));
    }

    @Test
    void get_userExists_shouldReturnUserMetadata() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        ChatUserMetadata returnedUserMetadata = chatSessionManager.get(chatId, "username");

        assertEquals(1, chatSessionManager.size());
        assertEquals(userMetadata, returnedUserMetadata);
    }

    @Test
    void get_chatRoomExists_shouldReturnChatRoomMetadata() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        ChatRoomMetadata metadata = chatSessionManager.get(chatId);

        assertEquals(1, chatSessionManager.size());
        assertEquals(1, metadata.size());
        assertEquals(userMetadata, metadata.get("username"));
    }

    @Test
    void computeIfAbsent_shouldCreateNewChatRoomWhenNotPresent() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata("username", ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        ChatRoomMetadata metadata = chatSessionManager.get(chatId);
        assertEquals(1, chatSessionManager.size());
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

        assertEquals(1, chatSessionManager.size());
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

        assertEquals(1, chatSessionManager.size());
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

        assertEquals(1, chatSessionManager.size());
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

        assertEquals(0, chatSessionManager.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfPresent_userNotInChatRoom_shouldReturnEmpty() {
        Long chatId = 1L;
        String username = "username";
        ChatUserMetadata userMetadata = new ChatUserMetadata(username, ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfPresent(chatId, "user1");

        assertEquals(1, chatSessionManager.size());
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

        assertEquals(1, chatSessionManager.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfPresent_userExists_shouldReturnUserMetadata() {
        Long chatId = 1L;
        String username = "username";
        ChatUserMetadata userMetadata = new ChatUserMetadata(username, ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfPresent(chatId, username);

        assertEquals(0, chatSessionManager.size());
        assertTrue(result.isPresent());
    }

    @Test
    void computeIfAbsent_concurrentAccess_shouldNotOverrideChatRoomMetadata() throws InterruptedException {
        ChatSessionManager chatSessionManager = new ChatSessionManager();
        Long chatId = 1L;
        int numberOfThreads = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<ChatUserMetadata> createdChatUserMetadata = Collections.synchronizedList(new ArrayList<>(2*numberOfThreads));

        // Submit 100 tasks that all try to create or add to the same chat room
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                ChatUserMetadata userMetadata = new ChatUserMetadata("username" + index, ZoneId.of("UTC"));
                chatSessionManager.computeIfAbsent(chatId, () -> {
                    createdChatUserMetadata.add(userMetadata);  // Store the created metadata instance
                    return userMetadata;
                });
            });
        }

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

        // Verify that only two users are in the chat room since it's supposed to hold only two
        ChatRoomMetadata metadata = chatSessionManager.get(chatId);
        assertEquals(2, metadata.size());

        // Check that the metadata in chatSubscriptions is the first one created
        for (int i = 1; i < createdChatUserMetadata.size(); i++) {
            assertTrue(metadata.contains(createdChatUserMetadata.get(i).getUsername()));
        }
    }

    @Test
    void removeIfPresent_removeBothUsers_concurrentAccess_shouldHandleMultipleThreads() throws InterruptedException {
        ChatSessionManager chatSessionManager = new ChatSessionManager();
        Long chatId = 1L;
        int threadCount = 100;
        String username1 = "username1";
        String username2 = "username2";
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(username1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(username2, ZoneId.of("UTC"));

        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata1);
        chatSessionManager.computeIfAbsent(chatId, () -> userMetadata2);

        // Create a thread pool to simulate concurrent access
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<ChatUserMetadata> successfulRemovals = Collections.synchronizedList(new ArrayList<>(2));

        // Submit tasks that try to remove users concurrently
        assertEquals(1, chatSessionManager.size());
        assertEquals(2, chatSessionManager.get(chatId).size());
        for (int i = 0; i < threadCount; i++) {
            final String usernameToRemove = (i % 2 == 0) ? username1 : username2;
            executorService.submit(() -> {
                Optional<ChatUserMetadata> removedInstance = chatSessionManager.removeIfPresent(chatId, usernameToRemove);
                removedInstance.ifPresent(successfulRemovals::add);
            });
        }

        // Shut down the executor and wait for all tasks to complete
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

        // Verify that the chat room is empty after removals
        assertEquals(0, chatSessionManager.size());

        // Verify that all removals were successful
        assertEquals(2, successfulRemovals.size());
        for (ChatUserMetadata removedInstance : successfulRemovals) {
            assertTrue(removedInstance.getUsername().equals(username1) || removedInstance.getUsername().equals(username2));
        }
    }
}

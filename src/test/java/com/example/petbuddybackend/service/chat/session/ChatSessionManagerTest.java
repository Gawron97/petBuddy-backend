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

    private static final String USERNAME_1 = "username1";
    private static final String USERNAME_2 = "username2";
    private static final String SESSION_ID_1 = "sessionId1";
    private static final String SESSION_ID_2 = "sessionId2";

    private ChatSessionManager chatSessionManager;

    @BeforeEach
    void setUp() {
        chatSessionManager = new ChatSessionManager();
    }

    @Test
    void get_chatRoomDoesNotExist_shouldReturnOptionalEmpty() {
        Long chatId = 1L;

        assertTrue(chatSessionManager.find(chatId).isEmpty());
    }

    @Test
    void get_givenUsernameNotInChatRoom_shouldReturnOptionalEmpty() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        assertEquals(1, chatSessionManager.size());
        assertTrue(chatSessionManager.find(chatId, USERNAME_2, SESSION_ID_1).isEmpty());
    }

    @Test
    void get_givenSessionIdNotInChatRoom_shouldThrowIllegalArgumentException() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        assertEquals(1, chatSessionManager.size());
        assertThrows(IllegalArgumentException.class, () -> chatSessionManager.find(chatId, USERNAME_1, SESSION_ID_2));
    }

    @Test
    void get_userExists_shouldReturnUserMetadata() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        ChatUserMetadata returnedUserMetadata = chatSessionManager.find(chatId, USERNAME_1, SESSION_ID_1).get();

        assertEquals(1, chatSessionManager.size());
        assertEquals(userMetadata, returnedUserMetadata);
    }

    @Test
    void get_userHasMultipleSessions_shouldReturnMetadataForEachSession() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_1, SESSION_ID_2, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);
        ChatUserMetadata returnedUserMetadata1 = chatSessionManager.find(chatId, USERNAME_1, SESSION_ID_1).get();
        ChatUserMetadata returnedUserMetadata2 = chatSessionManager.find(chatId, USERNAME_1, SESSION_ID_2).get();

        assertEquals(1, chatSessionManager.size());
        assertEquals(userMetadata1, returnedUserMetadata1);
        assertEquals(userMetadata2, returnedUserMetadata2);
    }

    @Test
    void put_shouldCreateNewChatRoom() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();

        assertEquals(1, chatSessionManager.size());
        assertEquals(userMetadata, metadata.get(USERNAME_1, SESSION_ID_1));
    }

    @Test
    void put_firstUserAlreadyExists_shouldOverride() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("Europe/Warsaw"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);

        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();
        ChatUserMetadata returnedUserMetadata = metadata.get(USERNAME_1, SESSION_ID_1);

        assertEquals(1, chatSessionManager.size());
        assertNotEquals(userMetadata1, returnedUserMetadata);
        assertEquals(userMetadata2, returnedUserMetadata);
    }

    @Test
    void put_secondUserAlreadyExists_shouldOverride() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_2, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata3 = new ChatUserMetadata(USERNAME_2, SESSION_ID_1, ZoneId.of("Europe/Warsaw"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);
        chatSessionManager.put(chatId, userMetadata3);

        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();
        ChatUserMetadata returnedUserMetadata = metadata.get(USERNAME_2, SESSION_ID_1);

        assertEquals(1, chatSessionManager.size());
        assertNotEquals(userMetadata2, returnedUserMetadata);
        assertEquals(userMetadata3, returnedUserMetadata);
    }

    @Test
    void put_userNotExistsInExistingChatRoom_shouldBeAdded() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_2, SESSION_ID_1, ZoneId.of("Europe/Warsaw"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);

        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();

        assertEquals(1, chatSessionManager.size());
        assertEquals(userMetadata1, metadata.get(USERNAME_1, SESSION_ID_1));
        assertEquals(userMetadata2, metadata.get(USERNAME_2, SESSION_ID_1));
    }

    @Test
    void put_firstUserGetsReaddedAfterRemove_shouldPopulateChatRoom() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_2, SESSION_ID_1, ZoneId.of("Europe/Warsaw"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);

        chatSessionManager.removeIfExists(chatId, USERNAME_1, SESSION_ID_1);
        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();

        assertEquals(1, chatSessionManager.size());
        assertNull(metadata.getFirstUserUsername());

        chatSessionManager.put(chatId, userMetadata1);
        metadata = chatSessionManager.find(chatId).get();

        assertEquals(1, chatSessionManager.size());
        assertEquals(userMetadata1, metadata.get(USERNAME_1, SESSION_ID_1));
        assertEquals(userMetadata2, metadata.get(USERNAME_2, SESSION_ID_1));
    }

    @Test
    void put_chatRoomIsFull_shouldThrowIllegalArgumentException() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_2, SESSION_ID_1, ZoneId.of("Europe/Warsaw"));
        ChatUserMetadata userMetadata3 = new ChatUserMetadata("someOtherUsername", SESSION_ID_1, ZoneId.of("Europe/Warsaw"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);
        assertThrows(IllegalStateException.class, () -> chatSessionManager.put(chatId, userMetadata3));
    }

    @Test
    void removeIfExists_chatRoomDoesNotExist_shouldReturnEmpty() {
        Long chatId = 1L;
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfExists(chatId, USERNAME_2, SESSION_ID_1);

        assertEquals(0, chatSessionManager.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfExists_userNotInChatRoom_shouldReturnEmpty() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfExists(chatId, USERNAME_2, SESSION_ID_1);

        assertEquals(1, chatSessionManager.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfExists_nonExistingSessionProvided_shouldReturnEmpty() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfExists(chatId, USERNAME_1, USERNAME_1);

        assertEquals(1, chatSessionManager.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfExists_userNotInProvidedChatRoom_shouldReturnEmpty() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata1);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfExists(chatId, USERNAME_2, SESSION_ID_1);

        assertEquals(1, chatSessionManager.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void removeIfExists_userExists_shouldReturnUserMetadata() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata);
        Optional<ChatUserMetadata> result = chatSessionManager.removeIfExists(chatId, USERNAME_1, SESSION_ID_1);

        assertEquals(0, chatSessionManager.size());
        assertTrue(result.isPresent());
    }

    @Test
    void removeIfPresent_userWithMultipleSessionsExist_shouldReturnMetadataFromAllSessions() {
        Long chatId = 1L;
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_1, SESSION_ID_2, ZoneId.of("Europe/Warsaw"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);
        Optional<ChatUserMetadata> result1 = chatSessionManager.removeIfExists(chatId, USERNAME_1, SESSION_ID_1);
        Optional<ChatUserMetadata> result2 = chatSessionManager.removeIfExists(chatId, USERNAME_1, SESSION_ID_2);

        assertEquals(0, chatSessionManager.size());
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(userMetadata1, result1.get());
        assertEquals(userMetadata2, result2.get());
    }

    @Test
    void put_concurrentAccess_shouldNotOverrideChatRoomMetadata() throws InterruptedException {
        ChatSessionManager chatSessionManager = new ChatSessionManager();
        Long chatId = 1L;
        int numberOfThreads = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<ChatUserMetadata> createdChatUserMetadata = Collections.synchronizedList(new ArrayList<>(2*numberOfThreads));

        // Submit 100 tasks that all try to create or add to the same chat room
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                ChatUserMetadata userMetadata = new ChatUserMetadata(USERNAME_1 + index, SESSION_ID_1, ZoneId.of("UTC"));
                ChatUserMetadata returnedMetadata = chatSessionManager.put(chatId, userMetadata);

                // If the metadata is the same as the one returned, it means it was created
                if(userMetadata.equals(returnedMetadata)) {
                    createdChatUserMetadata.add(userMetadata);
                }
            });
        }

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

        // Verify that only two users are in the chat room since it's supposed to hold only two
        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();
        assertEquals(1, metadata.firstUserSessionsSize());
        assertEquals(1, metadata.secondUserSessionsSize());

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
        ChatUserMetadata userMetadata1 = new ChatUserMetadata(USERNAME_1, SESSION_ID_1, ZoneId.of("UTC"));
        ChatUserMetadata userMetadata2 = new ChatUserMetadata(USERNAME_2, SESSION_ID_2, ZoneId.of("UTC"));

        chatSessionManager.put(chatId, userMetadata1);
        chatSessionManager.put(chatId, userMetadata2);

        // Create a thread pool to simulate concurrent access
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<ChatUserMetadata> successfulRemovals = Collections.synchronizedList(new ArrayList<>(2));

        // Submit tasks that try to remove users concurrently
        ChatRoomSessionMetadata metadata = chatSessionManager.find(chatId).get();
        assertEquals(1, chatSessionManager.size());
        assertEquals(1, metadata.firstUserSessionsSize());
        assertEquals(1, metadata.secondUserSessionsSize());

        for (int i = 0; i < threadCount; i++) {
            final String usernameToRemove = (i % 2 == 0) ? USERNAME_1 : USERNAME_2;
            final String sessionToRemove = (i % 2 == 0) ? SESSION_ID_1 : SESSION_ID_2;

            executorService.submit(() -> {
                Optional<ChatUserMetadata> removedInstance = chatSessionManager.removeIfExists(chatId, usernameToRemove, sessionToRemove);
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
            assertTrue(
                    removedInstance.getUsername().equals(USERNAME_1) ||
                    removedInstance.getUsername().equals(USERNAME_2)
            );
        }
    }
}

package com.example.petbuddybackend.service.chat.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatRoomSessionMetadataTest {

    private static final String USER1 = "user1";
    private static final String USER2 = "user2";
    private static final String SESSION1 = "session1";
    private static final String SESSION2 = "session2";

    private ChatUserMetadata user1;
    private ChatUserMetadata user2;

    @BeforeEach
    void setUp() {
        user1 = mock(ChatUserMetadata.class);
        user2 = mock(ChatUserMetadata.class);

        when(user1.getUsername()).thenReturn(USER1);
        when(user2.getUsername()).thenReturn(USER2);
        when(user1.getSessionId()).thenReturn(SESSION1);
        when(user2.getSessionId()).thenReturn(SESSION2);
    }

    @Test
    void put_shouldAddUser() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata();

        room.put(user1);

        assertEquals(1, room.firstUserSessionsSize());
        assertTrue(room.contains(USER1));
    }

    @Test
    void put_shouldAddSecondUser() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);

        room.put(user2);

        assertEquals(1, room.firstUserSessionsSize());
        assertEquals(1, room.secondUserSessionsSize());
        assertTrue(room.contains(USER2));
    }

    @Test
    void put_shouldThrowExceptionIfRoomIsFull() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);
        room.put(user2);

        ChatUserMetadata user3 = mock(ChatUserMetadata.class);
        when(user3.getUsername()).thenReturn("user3");

        assertThrows(IllegalStateException.class, () -> room.put(user3));
    }

    @Test
    void get_shouldReturnUserIfExists() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);
        room.put(user2);

        assertEquals(user1, room.get(USER1, SESSION1));
        assertEquals(user2, room.get(USER2, SESSION2));
    }

    @Test
    void get_shouldThrowExceptionIfUserNotFound() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);

        assertThrows(IllegalArgumentException.class, () -> room.get(USER2, SESSION2));
    }

    @Test
    void remove_shouldRemoveFirstUserAndReturnMetadata() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1, user2);
        ChatUserMetadata removed = room.removeIfExists(USER1, SESSION1).get();

        assertEquals(user1, removed);
        assertFalse(room.contains(USER1));
        assertEquals(0, room.firstUserSessionsSize());
        assertEquals(1, room.secondUserSessionsSize());
    }

    @Test
    void remove_shouldRemoveSecondUserAndReturnMetadata() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1, user2);
        ChatUserMetadata removed = room.removeIfExists(USER2, SESSION2).get();

        assertEquals(user2, removed);
        assertFalse(room.contains(USER2));
        assertEquals(1, room.firstUserSessionsSize());
        assertEquals(0, room.secondUserSessionsSize());
    }

    @Test
    void sessionSize_shouldReturnCorrectSize() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata();
        assertEquals(0, room.firstUserSessionsSize());
        assertEquals(0, room.secondUserSessionsSize());

        room.put(user1);
        assertEquals(1, room.firstUserSessionsSize());
        assertEquals(0, room.secondUserSessionsSize());

        room.put(user2);
        assertEquals(1, room.firstUserSessionsSize());
        assertEquals(1, room.secondUserSessionsSize());
    }

    @Test
    void contains_shouldReturnTrueIfUserExists() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);

        assertTrue(room.contains(USER1));
        assertFalse(room.contains(USER2));
    }

    @Test
    void isEmpty_shouldReturnTrueIfRoomIsNotFull() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata();
        assertTrue(room.isEmpty());
    }

    @Test
    void isEmpty_shouldReturnFalseIfRoomHasUsers() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);
        assertFalse(room.isEmpty());
    }

    @Test
    void isFull_shouldReturnTrueIfRoomIsFull() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);
        room.put(user2);

        assertTrue(room.isPopulated());
    }

    @Test
    void isFull_shouldReturnFalseIfRoomIsNotFull() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);

        assertFalse(room.isPopulated());
    }

    @Test
    void iterator_shouldIterateOverUsers() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1, user2);
        Iterator<ChatUserMetadata> iterator = room.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(user1, iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(user2, iterator.next());

        assertFalse(iterator.hasNext());
    }

    @Test
    void iterator_shouldThrowExceptionWhenNoMoreElements() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata(user1);

        Iterator<ChatUserMetadata> iterator = room.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(user1, iterator.next());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorHasNext_emptyChatRoom_shouldReturnFalse() {
        ChatRoomSessionMetadata room = new ChatRoomSessionMetadata();
        Iterator<ChatUserMetadata> iterator = room.iterator();

        assertFalse(iterator.hasNext());
    }
}

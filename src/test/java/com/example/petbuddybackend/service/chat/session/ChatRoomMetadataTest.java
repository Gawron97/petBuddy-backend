package com.example.petbuddybackend.service.chat.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatRoomMetadataTest {

    private ChatUserMetadata user1;
    private ChatUserMetadata user2;

    @BeforeEach
    void setUp() {
        user1 = mock(ChatUserMetadata.class);
        user2 = mock(ChatUserMetadata.class);

        when(user1.getUsername()).thenReturn("user1");
        when(user2.getUsername()).thenReturn("user2");
    }

    @Test
    void add_shouldAddUser() {
        ChatRoomMetadata room = new ChatRoomMetadata();

        room.add(user1);

        assertEquals(1, room.size());
        assertTrue(room.contains("user1"));
    }

    @Test
    void add_shouldAddSecondUser() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);

        room.add(user2);

        assertEquals(2, room.size());
        assertTrue(room.contains("user2"));
    }

    @Test
    void add_shouldThrowExceptionIfRoomIsFull() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);
        room.add(user2);

        ChatUserMetadata user3 = mock(ChatUserMetadata.class);
        when(user3.getUsername()).thenReturn("user3");

        assertThrows(IllegalStateException.class, () -> room.add(user3));
    }

    @Test
    void get_shouldReturnUserIfExists() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);
        room.add(user2);

        assertEquals(user1, room.get("user1"));
        assertEquals(user2, room.get("user2"));
    }

    @Test
    void get_shouldThrowExceptionIfUserNotFound() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);

        assertThrows(IllegalArgumentException.class, () -> room.get("user2"));
    }

    @Test
    void remove_shouldRemoveFirstUserAndReturnMetadata() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1, user2);
        ChatUserMetadata removed = room.remove("user1");

        assertEquals(user1, removed);
        assertFalse(room.contains("user1"));
        assertEquals(1, room.size());
    }

    @Test
    void remove_shouldRemoveSecondUserAndReturnMetadata() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1, user2);
        ChatUserMetadata removed = room.remove("user2");

        assertEquals(user2, removed);
        assertFalse(room.contains("user2"));
        assertEquals(1, room.size());
    }

    @Test
    void remove_shouldThrowExceptionIfUserNotFound() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);

        assertThrows(IllegalArgumentException.class, () -> room.remove("user2"));
    }

    @Test
    void size_shouldReturnCorrectSize() {
        ChatRoomMetadata room = new ChatRoomMetadata();
        assertEquals(0, room.size());

        room.add(user1);
        assertEquals(1, room.size());

        room.add(user2);
        assertEquals(2, room.size());
    }

    @Test
    void contains_shouldReturnTrueIfUserExists() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);

        assertTrue(room.contains("user1"));
        assertFalse(room.contains("user2"));
    }

    @Test
    void isEmpty_shouldReturnTrueIfRoomIsNotFull() {
        ChatRoomMetadata room = new ChatRoomMetadata();
        assertTrue(room.isEmpty());
    }

    @Test
    void isEmpty_shouldReturnFalseIfRoomHasUsers() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);
        assertFalse(room.isEmpty());
    }

    @Test
    void isFull_shouldReturnTrueIfRoomIsFull() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);
        room.add(user2);

        assertTrue(room.isFull());
    }

    @Test
    void isFull_shouldReturnFalseIfRoomIsNotFull() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);

        assertFalse(room.isFull());
    }

    @Test
    void iterator_shouldIterateOverUsers() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1, user2);
        Iterator<ChatUserMetadata> iterator = room.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(user1, iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(user2, iterator.next());

        assertFalse(iterator.hasNext());
    }

    @Test
    void iterator_shouldThrowExceptionWhenNoMoreElements() {
        ChatRoomMetadata room = new ChatRoomMetadata(user1);

        Iterator<ChatUserMetadata> iterator = room.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(user1, iterator.next());

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorHasNext_emptyChatRoom_shouldReturnFalse() {
        ChatRoomMetadata room = new ChatRoomMetadata();
        Iterator<ChatUserMetadata> iterator = room.iterator();

        assertFalse(iterator.hasNext());
    }
}

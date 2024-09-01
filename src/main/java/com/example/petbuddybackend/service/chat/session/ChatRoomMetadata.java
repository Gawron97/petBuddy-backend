package com.example.petbuddybackend.service.chat.session;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;

@Data
@NoArgsConstructor
class ChatRoomMetadata implements Iterable<ChatUserMetadata> {

    private static final String USER_NOT_FOUND_IN_CHAT_ROOM_MESSAGE = "User not found in chat room";
    private static final String CHAT_ROOM_IS_FULL_MESSAGE = "Chat room is full";
    private static final String NO_MORE_ELEMENTS_IN_CHAT_ROOM_MESSAGE = "No more elements in chat room";

    private ChatUserMetadata firstUserMeta;
    private ChatUserMetadata secondUserMeta;
    private Semaphore semaphore;

    public ChatRoomMetadata(ChatUserMetadata first) {
        this.firstUserMeta = first;
        this.semaphore = new Semaphore(1);
    }

    public ChatRoomMetadata(ChatUserMetadata firstUserMeta, ChatUserMetadata secondUserMeta) {
        this(firstUserMeta);
        this.secondUserMeta = secondUserMeta;
    }

    public void add(ChatUserMetadata metadata) {
        if(firstUserMeta == null) {
            firstUserMeta = metadata;
        } else if(secondUserMeta == null) {
            secondUserMeta = metadata;
        } else {
            throw new IllegalStateException(CHAT_ROOM_IS_FULL_MESSAGE);
        }
    }

    public ChatUserMetadata get(String username) {
        if(firstUserMeta != null && firstUserMeta.getUsername().equals(username)) {
            return firstUserMeta;
        }

        if(secondUserMeta != null && secondUserMeta.getUsername().equals(username)) {
            return secondUserMeta;
        }

        throw new IllegalArgumentException(USER_NOT_FOUND_IN_CHAT_ROOM_MESSAGE);
    }

    public boolean contains(String username) {
        if(firstUserMeta != null && firstUserMeta.getUsername().equals(username)) {
            return true;
        }

        return secondUserMeta != null && secondUserMeta.getUsername().equals(username);
    }

    public int size() {
        if(firstUserMeta == null) {
            return 0;
        } else if(secondUserMeta == null) {
            return 1;
        } else {
            return 2;
        }
    }

    public boolean isFull() {
        return size() == 2;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public ChatUserMetadata remove(String username) {
        if (firstUserMeta != null && firstUserMeta.getUsername().equals(username)) {
            ChatUserMetadata metadata = firstUserMeta;
            firstUserMeta = secondUserMeta;
            secondUserMeta = null;
            return metadata;
        } else if(secondUserMeta != null && secondUserMeta.getUsername().equals(username)) {
            ChatUserMetadata metadata = secondUserMeta;
            secondUserMeta = null;
            return metadata;
        } else {
            throw new IllegalArgumentException(USER_NOT_FOUND_IN_CHAT_ROOM_MESSAGE);
        }
    }

    @Override
    public Iterator<ChatUserMetadata> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                if(firstUserMeta == null) {
                    return false;
                } else if(secondUserMeta == null) {
                    return index < 1;
                } else {
                    return index < 2;
                }
            }

            @Override
            public ChatUserMetadata next() {
                if(!hasNext()) {
                    throw new NoSuchElementException(NO_MORE_ELEMENTS_IN_CHAT_ROOM_MESSAGE);
                } else {
                    if(index == 0) {
                        index++;
                        return firstUserMeta;
                    } else {
                        index++;
                        return secondUserMeta;
                    }
                }
            }
        };
    }
}

package com.example.petbuddybackend.service.chat.session;

import lombok.*;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Stores {@link ChatUserMetadata} about a chat room for both of the users for each of their sessions.
 * It also contains a semaphore to control access to the chat room.
 * */
@ToString
@EqualsAndHashCode
@NoArgsConstructor
class ChatRoomSessionMetadata implements Iterable<ChatUserMetadata> {

    private static final int INITIAL_SESSION_CAPACITY = 1;
    private static final String USER_NOT_FOUND_IN_CHAT_ROOM_MESSAGE = "User \"%s\" with given session: \"%s\" not found in chat room";
    private static final String CHAT_ROOM_IS_FULL_MESSAGE = "Chat room is full and username \"%s\" does not match any of the users";
    private static final String NO_MORE_ELEMENTS_IN_CHAT_ROOM_MESSAGE = "No more elements in chat room";

    @Getter
    private Semaphore semaphore;

    @Nullable @Getter
    private String firstUserUsername;

    @Nullable @Getter
    private String secondUserUsername;

    private final Map<String, ChatUserMetadata> firstUserMetas = new HashMap<>(INITIAL_SESSION_CAPACITY);
    private final Map<String, ChatUserMetadata> secondUserMetas = new HashMap<>(INITIAL_SESSION_CAPACITY);

    public ChatRoomSessionMetadata(@NonNull ChatUserMetadata firstUserMeta) {
        this.semaphore = new Semaphore(1);
        this.firstUserMetas.put(firstUserMeta.getSessionId(), firstUserMeta);
        this.firstUserUsername = firstUserMeta.getUsername();
    }

    public ChatRoomSessionMetadata(
            @NonNull ChatUserMetadata firstUserMeta,
            @NonNull ChatUserMetadata secondUserMeta
    ) {
        this(firstUserMeta);
        this.secondUserUsername = secondUserMeta.getUsername();
        this.secondUserMetas.put(secondUserMeta.getSessionId(), secondUserMeta);
    }

    /**
     * Puts the user metadata in the chat room. If the chat room is full and the user is not part of it, an exception is
     * thrown. If provided metadata with matching username and sessionId is already in the chat room, then the object
     * gets updated.
     *
     * @throws IllegalStateException if the chat room is full and the user is not part of it.
     * */
    public ChatUserMetadata put(@NonNull ChatUserMetadata metadata) {
        String username = metadata.getUsername();

        if(username.equals(firstUserUsername)) {
            return firstUserMetas.put(metadata.getSessionId(), metadata);
        }

        if(username.equals(secondUserUsername)) {
            return secondUserMetas.put(metadata.getSessionId(), metadata);
        }

        if(firstUserUsername == null) {
            firstUserUsername = username;
            return firstUserMetas.put(metadata.getSessionId(), metadata);
        }

        if(secondUserUsername == null) {
            secondUserUsername = username;
            return secondUserMetas.put(metadata.getSessionId(), metadata);
        }

        throw new IllegalStateException(String.format(CHAT_ROOM_IS_FULL_MESSAGE, username));
    }

    /**
     * Gets the user metadata from the chat room.
     *
     * @throws IllegalArgumentException if the user is not found in the chat room.
     * */
    public ChatUserMetadata get(@NonNull String username, @NonNull String sessionId) {
        if(username.equals(firstUserUsername) && firstUserMetas.containsKey(sessionId)) {
            return firstUserMetas.get(sessionId);
        }

        if(username.equals(secondUserUsername) && secondUserMetas.containsKey(sessionId)) {
            return secondUserMetas.get(sessionId);
        }

        throw new IllegalArgumentException(String.format(USER_NOT_FOUND_IN_CHAT_ROOM_MESSAGE, username, sessionId));
    }

    public boolean contains(@NonNull String username) {
        return Objects.equals(username, firstUserUsername) || Objects.equals(username, secondUserUsername);
    }

    public boolean isPopulated() {
        return firstUserUsername != null && secondUserUsername != null;
    }

    public boolean isEmpty() {
        return firstUserUsername == null && secondUserUsername == null;
    }

    public int firstUserSessionsSize() {
        return firstUserMetas.size();
    }

    public int secondUserSessionsSize() {
        return secondUserMetas.size();
    }

    public Optional<ChatUserMetadata> removeIfExists(@NonNull String username, @NonNull String sessionId) {
        if(username.equals(firstUserUsername) && firstUserMetas.containsKey(sessionId)) {
            ChatUserMetadata removedMeta = firstUserMetas.remove(sessionId);

            if(firstUserMetas.isEmpty()) {
                firstUserUsername = null;
            }

            return Optional.of(removedMeta);
        }

        if(username.equals(secondUserUsername) && secondUserMetas.containsKey(sessionId)) {
            ChatUserMetadata removedMeta = secondUserMetas.remove(sessionId);

            if(secondUserMetas.isEmpty()) {
                secondUserUsername = null;
            }

            return Optional.of(removedMeta);
        }

        return Optional.empty();
    }

    @Override
    public Iterator<ChatUserMetadata> iterator() {
        return new ChatRoomSessionMetadataIterator();
    }

    private class ChatRoomSessionMetadataIterator implements Iterator<ChatUserMetadata> {

        private final int FIRST_SNAPSHOT_SIZE;
        private final int SECOND_SNAPSHOTS_SIZE;
        private final List<ChatUserMetadata> firstSnapshot;
        private final List<ChatUserMetadata> secondSnapshot;
        private int firstIndex;
        private int secondIndex;

        public ChatRoomSessionMetadataIterator() {
            this.firstSnapshot = List.copyOf(firstUserMetas.values());
            this.secondSnapshot = List.copyOf(secondUserMetas.values());
            this.FIRST_SNAPSHOT_SIZE = firstSnapshot.size();
            this.SECOND_SNAPSHOTS_SIZE = secondSnapshot.size();
            this.firstIndex = 0;
            this.secondIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return firstIndex < FIRST_SNAPSHOT_SIZE || secondIndex < SECOND_SNAPSHOTS_SIZE;
        }

        @Override
        public ChatUserMetadata next() {
            if(firstIndex < FIRST_SNAPSHOT_SIZE) {
                return firstSnapshot.get(firstIndex++);
            }

            if(secondIndex < SECOND_SNAPSHOTS_SIZE) {
                return secondSnapshot.get(secondIndex++);
            }

            throw new NoSuchElementException(NO_MORE_ELEMENTS_IN_CHAT_ROOM_MESSAGE);
        }
    }
}

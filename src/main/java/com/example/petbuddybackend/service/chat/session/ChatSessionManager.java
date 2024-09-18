package com.example.petbuddybackend.service.chat.session;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Stores {@link ChatRoomSessionMetadata} about chat rooms. It ensures thread safety per chat room instance.
 * */
@Component
@ToString
@EqualsAndHashCode
class ChatSessionManager {

    private final ConcurrentHashMap<Long, ChatRoomSessionMetadata> chatSubscriptions;

    public ChatSessionManager() {
        this.chatSubscriptions = new ConcurrentHashMap<>();
    }

    public Optional<ChatRoomSessionMetadata> find(Long chatId) {
        return Optional.ofNullable(chatSubscriptions.get(chatId));
    }

    public Optional<ChatUserMetadata> find(Long chatId, String username, String sessionId) {
        Optional<ChatRoomSessionMetadata> chatRoomMetaOpt = find(chatId);

        if(chatRoomMetaOpt.isEmpty()) {
            return Optional.empty();
        }

        ChatRoomSessionMetadata chatRoomMeta = chatRoomMetaOpt.get();

        if(chatRoomMeta.contains(username)) {
            return Optional.of(chatRoomMeta.get(username, sessionId));
        }

        return Optional.empty();
    }

    /**
     * @return the number of chat rooms
     */
    public int size() {
        return chatSubscriptions.size();
    }

    /**
     * Adds a user to the chat room if it is not full. If the chat room is full and the user is not part of it, an
     * exception is thrown.
     *
     * @throws IllegalStateException if the chat room is full and the user is not part of it
     * */
    public ChatUserMetadata put(Long chatId, ChatUserMetadata userMetadata) {
        if(!chatSubscriptions.containsKey(chatId)) {
            synchronized(chatSubscriptions) {
                if(!chatSubscriptions.containsKey(chatId)) {
                    chatSubscriptions.put(chatId, new ChatRoomSessionMetadata(userMetadata));
                    return userMetadata;
                }
            }
        }

        ChatRoomSessionMetadata metadata = chatSubscriptions.get(chatId);
        Semaphore semaphore = metadata.getSemaphore();

        try {
            semaphore.acquire();
            metadata.put(userMetadata);
            return userMetadata;
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            semaphore.release();
        }
    }

    public Optional<ChatUserMetadata> removeIfExists(Long chatId, String username, String sessionId) {
        ChatRoomSessionMetadata metadata = chatSubscriptions.get(chatId);

        if(metadata == null) {
            return Optional.empty();
        }

        Semaphore semaphore = metadata.getSemaphore();

        try {
            semaphore.acquire();

            if(!metadata.contains(username)) {
                return Optional.empty();
            }

            Optional<ChatUserMetadata> userMetadata = metadata.removeIfExists(username, sessionId);

            if(metadata.isEmpty()) {
                chatSubscriptions.remove(chatId);
            }

            return userMetadata;

        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            semaphore.release();
        }
    }
}

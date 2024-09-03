package com.example.petbuddybackend.service.chat.session;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Stores {@link ChatRoomMetadata} about chat rooms. It ensures thread safety per chat room instance.
 * */
@Component
@ToString
@EqualsAndHashCode
class ChatSessionManager {

    public static final String ILLEGAL_ACCESS_TO_FULL_CHAT_ROOM_MESSAGE = "Illegal access to full chat room: user not found in chat room";
    private final Map<Long, ChatRoomMetadata> chatSubscriptions;

    public ChatSessionManager() {
        this.chatSubscriptions = new ConcurrentHashMap<>();
    }

    public ChatRoomMetadata get(Long chatId) {
        ChatRoomMetadata chatUserMetadata = chatSubscriptions.get(chatId);

        if(chatUserMetadata == null) {
            throw new IllegalArgumentException("Chat room not found");
        }

        return chatUserMetadata;
    }

    public ChatUserMetadata get(Long chatId, String username) {
        return get(chatId).get(username);
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
    public ChatUserMetadata putIfAbsent(Long chatId, ChatUserMetadata userMetadata) {
        if(!chatSubscriptions.containsKey(chatId)) {
            synchronized(chatSubscriptions) {
                if(!chatSubscriptions.containsKey(chatId)) {
                    chatSubscriptions.put(chatId, new ChatRoomMetadata(userMetadata));
                    return userMetadata;
                }
            }
        }

        ChatRoomMetadata metadata = chatSubscriptions.get(chatId);
        Semaphore semaphore = metadata.getSemaphore();

        try {
            semaphore.acquire();

            if(metadata.isFull()) {
                try {
                    return metadata.get(userMetadata.getUsername());
                } catch(IllegalArgumentException e) {
                    throw new IllegalStateException(ILLEGAL_ACCESS_TO_FULL_CHAT_ROOM_MESSAGE);
                }
            }

            if(!metadata.contains(userMetadata.getUsername())) {
                metadata.add(userMetadata);
                return userMetadata;
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            semaphore.release();
        }

        return metadata.get(userMetadata.getUsername());
    }

    public Optional<ChatUserMetadata> remove(Long chatId, String username) {
        ChatRoomMetadata metadata = chatSubscriptions.get(chatId);

        if(metadata == null) {
            return Optional.empty();
        }

        Semaphore semaphore = metadata.getSemaphore();

        try {
            semaphore.acquire();

            if(!metadata.contains(username)) {
                return Optional.empty();
            }

            ChatUserMetadata userMetadata = metadata.remove(username);
            if(metadata.isEmpty()) {
                chatSubscriptions.remove(chatId);
            }

            System.out.println(Thread.currentThread().getId() + ": Returning removed user" + userMetadata.getUsername());
            return Optional.of(userMetadata);

        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            semaphore.release();
        }
    }
}

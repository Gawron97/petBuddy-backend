package com.example.petbuddybackend.service.chat.session;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

@ToString
@Component
@EqualsAndHashCode
class ChatSessionManager {

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
     * Computes the metadata if the chat room does not exist, or adds the user to the chat room if it exists.
     * */
    public void computeIfAbsent(Long chatId, Supplier<ChatUserMetadata> userMetadataProvider) {
        if(!chatSubscriptions.containsKey(chatId)) {
            synchronized(chatSubscriptions) {
                if(!chatSubscriptions.containsKey(chatId)) {
                    ChatUserMetadata userMetadata = userMetadataProvider.get();
                    chatSubscriptions.put(chatId, new ChatRoomMetadata(userMetadata));
                    return;
                }
            }
        }

        ChatRoomMetadata metadata = chatSubscriptions.get(chatId);
        Semaphore semaphore = metadata.getSemaphore();

        try {
            semaphore.acquire();
            if(metadata.isFull()) {
                return;
            }

            ChatUserMetadata userMetadata = userMetadataProvider.get();
            if(!metadata.contains(userMetadata.getUsername())) {
                metadata.add(userMetadata);
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaphore.release();
        }
    }

    public Optional<ChatUserMetadata> removeIfPresent(Long chatId, String username) {
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

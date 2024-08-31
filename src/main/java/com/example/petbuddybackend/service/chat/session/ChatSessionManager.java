package com.example.petbuddybackend.service.chat.session;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// TODO: fix concurrency issues
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

    // FIXME: probably not concurrent
    /**
     * Adds a user to a chat room if the chat room does not exist or the existing one is not full.
     * If the chat room is full, the user is not added.
     * */
    public void computeIfAbsent(Long chatId, Supplier<ChatUserMetadata> userMetadataProvider) {
        if(!chatSubscriptions.containsKey(chatId)) {
            ChatUserMetadata userMetadata = userMetadataProvider.get();
            chatSubscriptions.put(chatId, new ChatRoomMetadata(userMetadata));
            return;
        }

        ChatRoomMetadata metadata = chatSubscriptions.get(chatId);
        if(metadata.isFull()) {
            return;
        }

        ChatUserMetadata userMetadata = userMetadataProvider.get();
        if(!metadata.contains(userMetadata.getUsername())) {
            metadata.add(userMetadata);
        }
    }

    // FIXME: probably not concurrent
    public Optional<ChatUserMetadata> removeIfPresent(Long chatId, String username) {
        if(!chatSubscriptions.containsKey(chatId)) {
            return Optional.empty();
        }

        ChatRoomMetadata metadata = chatSubscriptions.get(chatId);
        if(!metadata.contains(username)) {
            return Optional.empty();
        }

        ChatUserMetadata userMetadata = metadata.remove(username);
        if(metadata.isEmpty()) {
            chatSubscriptions.remove(chatId);
        }

        return Optional.of(userMetadata);
    }
}

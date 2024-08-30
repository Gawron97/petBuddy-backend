package com.example.petbuddybackend.service.chat.session;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ToString
@EqualsAndHashCode
class ChatSessionManager {

    private final Map<Long, ChatRoomMetadata> chatSubscriptions;

    public ChatSessionManager() {
        this.chatSubscriptions = new ConcurrentHashMap<>();
    }

    public ChatUserMetadata get(Long chatId, String username) {
        return chatSubscriptions.get(chatId).get(username);
    }

    public ChatRoomMetadata get(Long chatId) {
        return chatSubscriptions.get(chatId);
    }

    /**
     * Creates a new entry if the chat room does not exist or if it is not full.
     * */
    public void createIfAbsent(Long chatId, Supplier<ChatUserMetadata> userMetadataProvider) {
        if(chatSubscriptions.containsKey(chatId)) {
            ChatRoomMetadata metadata = chatSubscriptions.get(chatId);
            if(!metadata.isFull()) {
                metadata.add(userMetadataProvider.get());
                return;
            }
        }
        chatSubscriptions.put(chatId, new ChatRoomMetadata(userMetadataProvider.get()));
    }

    public Optional<ChatUserMetadata> removeIfPresent(Long chatId, String username) {
        if(!chatSubscriptions.containsKey(chatId)) {
            return Optional.empty();
        }

        ChatRoomMetadata metadata = chatSubscriptions.get(chatId);

        if(!metadata.contains(username)) {
            return Optional.empty();
        }

        return Optional.of(metadata.remove(username));
    }
}

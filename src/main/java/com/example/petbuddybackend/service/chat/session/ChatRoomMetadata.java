package com.example.petbuddybackend.service.chat.session;

import lombok.Data;

import java.util.Iterator;

@Data
class ChatRoomMetadata implements Iterable<ChatUserMetadata> {

    private ChatUserMetadata firstUserMeta;
    private ChatUserMetadata secondUserMeta;

    public ChatRoomMetadata(ChatUserMetadata first, ChatUserMetadata second) {
        this.firstUserMeta = first;
        this.secondUserMeta = second;
    }

    public ChatRoomMetadata(ChatUserMetadata first) {
        this.firstUserMeta = first;
    }

    public ChatUserMetadata get(String username) {
        if (firstUserMeta.getUsername().equals(username)) {
            return firstUserMeta;
        } else if (secondUserMeta.getUsername().equals(username)) {
            return secondUserMeta;
        }

        throw new IllegalArgumentException("User not found in chat room");
    }

    public boolean isFull() {
        return firstUserMeta != null && secondUserMeta != null;
    }

    public boolean contains(String username) {
        return firstUserMeta.getUsername().equals(username) || secondUserMeta.getUsername().equals(username);
    }

    public void add(ChatUserMetadata metadata) {
        if (firstUserMeta == null) {
            firstUserMeta = metadata;
        } else if (secondUserMeta == null) {
            secondUserMeta = metadata;
        } else {
            throw new IllegalStateException("Chat room is full");
        }
    }

    public ChatUserMetadata remove(String username) {
        if (firstUserMeta.getUsername().equals(username)) {
            ChatUserMetadata metadata = firstUserMeta;
            firstUserMeta = secondUserMeta;
            return metadata;
        } else if (secondUserMeta.getUsername().equals(username)) {
            ChatUserMetadata metadata = secondUserMeta;
            secondUserMeta = null;
            return metadata;
        } else {
            throw new IllegalArgumentException("User not found in chat room");
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
                if(index == 0) {
                    index++;
                    return firstUserMeta;
                } else if(index == 1) {
                    index++;
                    return secondUserMeta;
                }

                throw new IndexOutOfBoundsException("No more elements in chat room");
            }
        };
    }
}

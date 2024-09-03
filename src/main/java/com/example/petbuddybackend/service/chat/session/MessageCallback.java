package com.example.petbuddybackend.service.chat.session;

@FunctionalInterface
public interface MessageCallback {
    void onMessageSent(String username);
}

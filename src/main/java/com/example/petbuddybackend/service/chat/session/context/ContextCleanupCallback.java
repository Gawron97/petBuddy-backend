package com.example.petbuddybackend.service.chat.session.context;

@FunctionalInterface
public interface ContextCleanupCallback {
    void onDestroy(Long chatId, String username, String sessionId);
}

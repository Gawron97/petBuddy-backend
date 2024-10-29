package com.example.petbuddybackend.service.chat.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChatSessionTracker {

    private final Map<String, Long> subsToChatId = new HashMap<>();

    public Set<String> getSubscriptionIds() {
        return subsToChatId.keySet();
    }

    public void addSubscription(String sessionId, Long chatId) {
        subsToChatId.put(sessionId, chatId);
    }

    public void removeSubscription(String sessionId) {
        subsToChatId.remove(sessionId);
    }

    public Long getChatId(String sessionId) {
        return subsToChatId.get(sessionId);
    }
}

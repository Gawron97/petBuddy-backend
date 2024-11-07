package com.example.petbuddybackend.service.chat.session;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Profile("!test")
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChatSessionTracker {

    private final Map<String, Long> subsToChatId = new HashMap<>();

    public Set<String> getSubscriptionIds() {
        return subsToChatId.keySet();
    }

    public Map<String, Long> getSubscriptions() {
        return Collections.unmodifiableMap(subsToChatId);
    }

    public void addSubscription(String sessionId, Long chatId) {
        subsToChatId.put(sessionId, chatId);
    }

    public Long removeSubscription(String sessionId) {
        return subsToChatId.remove(sessionId);
    }

    public Long getChatId(String sessionId) {
        return subsToChatId.get(sessionId);
    }

    public void clear() {
        subsToChatId.clear();
    }
}

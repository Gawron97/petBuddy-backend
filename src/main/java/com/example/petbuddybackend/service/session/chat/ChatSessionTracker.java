package com.example.petbuddybackend.service.session.chat;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!test")
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChatSessionTracker {

    private final Map<String, Long> subsToChatId = new HashMap<>();

    public Map<String, Long> getSubscriptions() {
        return Collections.unmodifiableMap(subsToChatId);
    }

    public void addSubscription(String subscriptionId, Long chatId) {
        subsToChatId.put(subscriptionId, chatId);
    }

    public Long removeSubscription(String subscriptionId) {
        return subsToChatId.remove(subscriptionId);
    }

    public void clear() {
        subsToChatId.clear();
    }
}

package com.example.petbuddybackend.service.chat.session.context;

import lombok.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@ToString
@Getter
@EqualsAndHashCode
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebSocketSessionContext implements DisposableBean {

    private Long chatId;
    private String username;
    private ContextCleanupCallback cleanupCallback;
    private boolean empty;
    private final Set<String> subscriptionIds;

    @Setter
    private String sessionId;

    public WebSocketSessionContext() {
        this.chatId = null;
        this.username = null;
        this.cleanupCallback = (chatId, username, sessionId) -> {};
        this.empty = true;
        this.subscriptionIds = new HashSet<>();
    }

    public WebSocketSessionContext(Long chatId, String username, ContextCleanupCallback cleanupCallback) {
        this.chatId = chatId;
        this.username = username;
        this.cleanupCallback = cleanupCallback;
        this.empty = false;
        this.subscriptionIds = new HashSet<>();
    }

    @Override
    public void destroy() {
        cleanupCallback.onDestroy(chatId, username, sessionId);
    }

    public void clearContext() {
        this.chatId = null;
        this.username = null;
        this.cleanupCallback = (id, username, sessionId) -> {};
        this.sessionId = null;
        this.empty = true;
    }

    public void setContext(Long chatId, String username, ContextCleanupCallback cleanupCallback) {
        this.chatId = chatId;
        this.username = username;
        this.cleanupCallback = cleanupCallback;
        this.empty = false;
    }

    public void addSubscriptionId(String subscriptionId) {
        subscriptionIds.add(subscriptionId);
    }

    public void removeSubscriptionId(String subscriptionId) {
        subscriptionIds.remove(subscriptionId);
    }

    public boolean containsSubscriptionId(String subscriptionId) {
        return subscriptionIds.contains(subscriptionId);
    }
}

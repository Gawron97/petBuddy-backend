package com.example.petbuddybackend.service.chat.session.context;

import lombok.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@ToString
@Getter
@EqualsAndHashCode
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ChatSessionContext implements DisposableBean {

    private Long chatId;
    private String username;
    private ContextCleanupCallback cleanupCallback;
    private boolean empty;

    public ChatSessionContext() {
        this.chatId = null;
        this.username = null;
        this.empty = false;
        this.cleanupCallback = (chatId, username) -> {};
    }

    public ChatSessionContext(Long chatId, String username, ContextCleanupCallback cleanupCallback) {
        this.chatId = chatId;
        this.username = username;
        this.cleanupCallback = cleanupCallback;
        this.empty = false;
    }

    @Override
    public void destroy() {
        cleanupCallback.onDestroy(chatId, username);
    }

    public void clearContext() {
        this.empty = true;
        this.username = null;
        this.chatId = null;
        this.cleanupCallback = (id, username) -> {};
    }

    public void setContext(Long chatId, String username, ContextCleanupCallback cleanupCallback) {
        this.empty = false;
        this.username = username;
        this.chatId = chatId;
        this.cleanupCallback = cleanupCallback;
    }
}

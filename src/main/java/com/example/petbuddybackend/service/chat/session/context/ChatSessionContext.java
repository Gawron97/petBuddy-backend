package com.example.petbuddybackend.service.chat.session.context;

import lombok.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Data
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
@AllArgsConstructor
public class ChatSessionContext implements DisposableBean {

    private Long chatId;
    private String username;
    private boolean userPresent;
    private ContextCleanupCallback cleanupCallback;

    public ChatSessionContext() {
        this.chatId = null;
        this.username = null;
        this.userPresent = true;
        this.cleanupCallback = (chatId, username) -> {};
    }

    @Override
    public void destroy() {
        if(chatId == null || username == null) {
            return;
        }

        cleanupCallback.onDestroy(chatId, username);
    }

    public boolean isEmpty() {
        return chatId == null && username == null;
    }
}

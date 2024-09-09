package com.example.petbuddybackend.service.chat.session.context;

import com.example.petbuddybackend.testutils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class ChatSessionContextTest {

    private ChatSessionContext chatSessionContext;
    private ContextCleanupCallback cleanupCallback;

    @BeforeEach
    void setUp() {
        cleanupCallback = Mockito.mock(ContextCleanupCallback.class);
        chatSessionContext = new ChatSessionContext(1L, "testUser", cleanupCallback);
    }

    @Test
    void testSetContext_shouldSetParams() {
        chatSessionContext.setContext(2L, "newUser", cleanupCallback);
        assertEquals(2L, chatSessionContext.getChatId());
        assertEquals("newUser", chatSessionContext.getUsername());
        assertFalse(chatSessionContext.isEmpty());
    }

    @Test
    void testClearContext_allParamsShouldBeNull() {
        chatSessionContext.clearContext();
        assertFalse(ValidationUtils.fieldsNotNullRecursive(chatSessionContext, Set.of("cleanupCallback")));
    }

    @Test
    void testDestroy_shouldCallOnDestroy() {
        chatSessionContext.destroy();
        verify(cleanupCallback).onDestroy(1L, "testUser");
    }

    @Test
    void testIsEmpty_afterClearContext_shouldReturnTrue() {
        chatSessionContext.clearContext();
        assertTrue(chatSessionContext.isEmpty());
    }

    @Test
    void testIsEmpty_contextWasSet_shouldReturnFalse() {
        chatSessionContext.setContext(3L, "testUser2", cleanupCallback);
        assertFalse(chatSessionContext.isEmpty());
    }
}

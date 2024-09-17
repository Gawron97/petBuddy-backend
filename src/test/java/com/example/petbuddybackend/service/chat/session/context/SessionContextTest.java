package com.example.petbuddybackend.service.chat.session.context;

import com.example.petbuddybackend.testutils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

public class SessionContextTest {

    private SessionContext chatSessionContext;
    private ContextCleanupCallback cleanupCallback;

    @BeforeEach
    void setUp() {
        cleanupCallback = Mockito.mock(ContextCleanupCallback.class);
        chatSessionContext = new SessionContext(1L, "testUser", cleanupCallback);
        chatSessionContext.setSessionId("sessionId");
    }

    @Test
    void testDefaultConstructor_initializesFields() {
        SessionContext context = new SessionContext();

        assertNull(context.getChatId());
        assertNull(context.getUsername());
        assertNotNull(context.getCleanupCallback());
        assertTrue(context.getSubscriptionIds().isEmpty());
        assertTrue(context.isEmpty());

        context.clearContext();
        assertDoesNotThrow(() -> context.getCleanupCallback().onDestroy(null, null, null));
    }

    @Test
    void testSetContext_shouldSetParams() {
        chatSessionContext.setContext(2L, "newUser", cleanupCallback);
        chatSessionContext.setSessionId("newSessionId");

        assertEquals(2L, chatSessionContext.getChatId());
        assertEquals("newUser", chatSessionContext.getUsername());
        assertEquals("newSessionId", chatSessionContext.getSessionId());
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
        verify(cleanupCallback).onDestroy(1L, "testUser", "sessionId");
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

    @Test
    void testAddSubscriptionId_shouldAddSubscription() {
        chatSessionContext.addSubscriptionId("sub1");
        chatSessionContext.addSubscriptionId("sub2");

        assertTrue(chatSessionContext.containsSubscriptionId("sub1"));
        assertTrue(chatSessionContext.containsSubscriptionId("sub2"));
    }

    @Test
    void testRemoveSubscriptionId_shouldRemoveSubscription() {
        chatSessionContext.addSubscriptionId("sub1");
        chatSessionContext.addSubscriptionId("sub2");
        chatSessionContext.removeSubscriptionId("sub1");

        assertFalse(chatSessionContext.containsSubscriptionId("sub1"));
        assertTrue(chatSessionContext.containsSubscriptionId("sub2"));
    }

    @Test
    void testContainsSubscriptionId_shouldReturnTrueForExistingSubscription() {
        chatSessionContext.addSubscriptionId("sub1");
        assertTrue(chatSessionContext.containsSubscriptionId("sub1"));
    }

    @Test
    void testContainsSubscriptionId_shouldReturnFalseForNonExistingSubscription() {
        assertFalse(chatSessionContext.containsSubscriptionId("sub1"));
    }
}

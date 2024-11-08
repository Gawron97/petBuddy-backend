package com.example.petbuddybackend.service.session.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTrackerTest {

    private ChatSessionTracker chatSessionTracker;

    @BeforeEach
    public void setUp() {
        chatSessionTracker = new ChatSessionTracker();
    }

    @Test
    public void testAddSubscription() {
        String subscriptionId = "sub1";
        Long chatId = 123L;

        chatSessionTracker.addSubscription(subscriptionId, chatId);

        Map<String, Long> subscriptions = chatSessionTracker.getSubscriptions();
        assertTrue(subscriptions.containsKey(subscriptionId));
        assertEquals(chatId, subscriptions.get(subscriptionId));
    }

    @Test
    public void testRemoveSubscription() {
        String subscriptionId = "sub1";
        Long chatId = 123L;
        chatSessionTracker.addSubscription(subscriptionId, chatId);

        Long removedChatId = chatSessionTracker.removeSubscription(subscriptionId);

        assertEquals(chatId, removedChatId);
        assertFalse(chatSessionTracker.getSubscriptions().containsKey(subscriptionId));
    }

    @Test
    public void testClearSubscriptions() {
        chatSessionTracker.addSubscription("sub1", 123L);
        chatSessionTracker.addSubscription("sub2", 456L);

        chatSessionTracker.clear();

        assertTrue(chatSessionTracker.getSubscriptions().isEmpty());
    }

    @Test
    public void testGetSubscriptionsImmutable() {
        chatSessionTracker.addSubscription("sub1", 123L);

        Map<String, Long> subscriptions = chatSessionTracker.getSubscriptions();
        assertThrows(UnsupportedOperationException.class, () -> subscriptions.put("sub2", 456L));
    }
}

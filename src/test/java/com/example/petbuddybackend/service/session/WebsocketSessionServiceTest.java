package com.example.petbuddybackend.service.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class WebsocketSessionServiceTest {

    private static final String USER_EMAIL = "userEmail";
    private static final String SESSION_ID = "sessionId123";
    private static final String USER_ZONE_ID = "Europe/Warsaw";

    @Value("${url.chat.topic.client-subscribe-pattern}")
    private String CHAT_TOPIC_URL_PATTERN;

    @Value("${url.chat.topic.subscribe-prefix}")
    private String CHAT_TOPIC_URL_PREFIX;

    @Autowired
    private WebSocketSessionService webSocketSessionService;

    @MockBean
    private SimpUserRegistry simpUserRegistry;

    private Field sessionsTimeZoneField;

    @BeforeEach
    public void setUp() throws Exception {
        sessionsTimeZoneField = WebSocketSessionService.class.getDeclaredField("sessionsTimeZone");
        sessionsTimeZoneField.setAccessible(true);
    }

    @Test
    public void testStoreUserTimeZoneWithSession() throws Exception {
        // When
        webSocketSessionService.storeUserTimeZoneWithSession(SESSION_ID, USER_ZONE_ID);

        // Then
        Map<String, ZoneId> sessionsTimeZone = (Map<String, ZoneId>) sessionsTimeZoneField.get(webSocketSessionService);
        assertEquals(ZoneId.of(USER_ZONE_ID), sessionsTimeZone.get(SESSION_ID));
    }

    @Test
    public void testRemoveUserSessionWithTimeZone() throws Exception {
        // When
        webSocketSessionService.storeUserTimeZoneWithSession(SESSION_ID, USER_ZONE_ID);
        webSocketSessionService.removeUserSessionWithTimeZone(SESSION_ID);

        // Then
        Map<String, ZoneId> sessionsTimeZone = (Map<String, ZoneId>) sessionsTimeZoneField.get(webSocketSessionService);
        assertFalse(sessionsTimeZone.containsKey(SESSION_ID));
    }

    @Test
    public void testGetNumberOfSessions() {
        // Given
        SimpUser simpUser = mock(SimpUser.class);
        SimpSession session1 = mock(SimpSession.class);
        SimpSession session2 = mock(SimpSession.class);
        Set<SimpSession> sessions = Set.of(session1, session2);

        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(sessions);

        // When
        Integer numberOfSessions = webSocketSessionService.getNumberOfSessions(USER_EMAIL);

        // Then
        assertEquals(2, numberOfSessions);
    }

    @Test
    public void testGetNumberOfSessions_UserNotConnected() {
        // Given
        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(null);

        // When
        Integer numberOfSessions = webSocketSessionService.getNumberOfSessions(USER_EMAIL);

        // Then
        assertEquals(0, numberOfSessions);
    }

    @Test
    public void testIsUserConnected_UserWithSessions() {
        // Given
        SimpUser simpUser = mock(SimpUser.class);
        SimpSession session1 = mock(SimpSession.class);
        Set<SimpSession> sessions = Set.of(session1);

        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(sessions);

        // When
        boolean isConnected = webSocketSessionService.isUserConnected(USER_EMAIL);

        // Then
        assertTrue(isConnected);
    }

    @Test
    public void testIsUserConnected_UserNotConnected() {
        // Given
        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(null);

        // When
        boolean isConnected = webSocketSessionService.isUserConnected(USER_EMAIL);

        // Then
        assertFalse(isConnected);
    }

    @Test
    public void testGetUserSessions_UserWithSessions() {
        // Given
        SimpUser simpUser = mock(SimpUser.class);
        SimpSession session1 = mock(SimpSession.class);
        SimpSession session2 = mock(SimpSession.class);
        Set<SimpSession> sessions = Set.of(session1, session2);

        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(sessions);

        // When
        Set<SimpSession> userSessions = webSocketSessionService.getUserSessions(USER_EMAIL);

        // Then
        assertEquals(2, userSessions.size());
    }

    @Test
    public void testGetUserSessions_UserNotFound() {
        // Given
        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(null);

        // When
        Set<SimpSession> userSessions = webSocketSessionService.getUserSessions(USER_EMAIL);

        // Then
        assertEquals(Collections.emptySet(), userSessions);
    }

    @Test
    public void testGetTimezoneOrDefault_WithSession() {
        // Given
        SimpSession session = mock(SimpSession.class);
        when(session.getId()).thenReturn(SESSION_ID);
        webSocketSessionService.storeUserTimeZoneWithSession(SESSION_ID, USER_ZONE_ID);

        // When
        ZoneId timezone = webSocketSessionService.getTimezoneOrDefault(session);

        // Then
        assertEquals(ZoneId.of(USER_ZONE_ID), timezone);
    }

    @Test
    public void testGetTimezoneOrDefault_WithSessionId() {
        // When
        webSocketSessionService.storeUserTimeZoneWithSession(SESSION_ID, USER_ZONE_ID);
        ZoneId timezone = webSocketSessionService.getTimezoneOrDefault(SESSION_ID);

        // Then
        assertEquals(ZoneId.of(USER_ZONE_ID), timezone);
    }

    @Test
    public void testGetTimezoneOrDefault_WithInvalidSessionId() {
        // When
        ZoneId timezone = webSocketSessionService.getTimezoneOrDefault("invalidSessionId");

        // Then
        assertEquals(ZoneId.systemDefault(), timezone);
    }

    @Test
    public void testGetUserSubscriptionStartingWithDestination() {
        // Given
        SimpUser simpUser = mock(SimpUser.class);
        SimpSession session1 = mock(SimpSession.class);
        SimpSubscription subscription1 = mock(SimpSubscription.class);
        SimpSubscription subscription2 = mock(SimpSubscription.class);

        // Mock destinations
        String matchingDestination = String.format(CHAT_TOPIC_URL_PATTERN, 123);
        String nonMatchingDestination = "/some/other/topic/456";

        // Mock user sessions and subscriptions
        Set<SimpSubscription> subscriptions = Set.of(subscription1, subscription2);
        Set<SimpSession> sessions = Set.of(session1);

        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(sessions);
        when(session1.getSubscriptions()).thenReturn(subscriptions);
        when(subscription1.getDestination()).thenReturn(matchingDestination);
        when(subscription2.getDestination()).thenReturn(nonMatchingDestination);

        // When
        Set<SimpSubscription> result = webSocketSessionService.getUserSubscriptionStartingWithDestination(USER_EMAIL, CHAT_TOPIC_URL_PREFIX);

        // Then
        assertEquals(1, result.size());
        assertEquals(matchingDestination, result.iterator().next().getDestination());
    }

    @Test
    public void testCountChatRoomSessions() {
        // Given
        SimpUser simpUser = mock(SimpUser.class);
        SimpSession session1 = mock(SimpSession.class);
        SimpSession session2 = mock(SimpSession.class);
        SimpSubscription subscription1 = mock(SimpSubscription.class);
        SimpSubscription subscription2 = mock(SimpSubscription.class);
        SimpSubscription subscription3 = mock(SimpSubscription.class);

        // Mock destinations
        String destination1 = String.format(CHAT_TOPIC_URL_PATTERN, 123);
        String destination2 = String.format(CHAT_TOPIC_URL_PATTERN, 123);
        String destination3 = String.format(CHAT_TOPIC_URL_PATTERN, 456);

        // Mock sessions and subscriptions
        Set<SimpSubscription> session1Subscriptions = Set.of(subscription1, subscription2);
        Set<SimpSubscription> session2Subscriptions = Set.of(subscription3);
        Set<SimpSession> sessions = Set.of(session1, session2);

        when(simpUserRegistry.getUser(eq(USER_EMAIL))).thenReturn(simpUser);
        when(simpUser.getSessions()).thenReturn(sessions);
        when(session1.getSubscriptions()).thenReturn(session1Subscriptions);
        when(session2.getSubscriptions()).thenReturn(session2Subscriptions);
        when(subscription1.getDestination()).thenReturn(destination1);
        when(subscription2.getDestination()).thenReturn(destination2);
        when(subscription3.getDestination()).thenReturn(destination3);

        // When
        Map<Long, Integer> result = webSocketSessionService.countChatRoomSessions(USER_EMAIL);

        // Then
        assertEquals(2, result.size());
        assertEquals(2, result.get(123L));
        assertEquals(1, result.get(456L));
    }
}

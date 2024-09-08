package com.example.petbuddybackend.utils;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.testutils.mock.GeneralMockProvider;
import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.*;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

public class HeaderUtilsTest {

    @Test
    void testGetHeaderSingleValue_validHeader_shouldSucceed() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("testHeader", "testValue");
        String result = HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class);
        assertEquals("testValue", result);
    }

    @Test
    void testGetHeaderSingleValue_missingHeader_throwsMissingWebSocketHeaderException() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("anotherHeader", "testValue");
        assertThrows(MissingWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class));
    }

    @Test
    void testGetHeaderSingleValue_invalidType_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("testHeader", "testValue");
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", Integer.class));
    }

    @Test
    void testGetHeaderSingleValue_emptyHeader_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithEmptyList("testHeader");
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class));
    }

    @Test
    void testGetHeaderSingleValue_multipleValuesInHeader_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithMultipleValues("testHeader", List.of("value1", "value2"));
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class));
    }

    @Test
    void testGetHeaderSingleValue_invalidHeaderStructure_shouldThrowIllegalArgumentException() {
        Map<String, Object> headers = Collections.emptyMap();
        assertThrows(IllegalArgumentException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class)
        );

    }

    @Test
    void testGetOptionalHeaderSingleValue_validHeader_shouldSucceed() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("testHeader", "testValue");
        Optional<String> result = HeaderUtils.getOptionalHeaderSingleValue(headers, "testHeader", String.class);
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get());
    }

    @Test
    void testGetOptionalHeaderSingleValue_missingHeader_shouldReturnEmptyOptional() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("anotherHeader", "testValue");
        Optional<String> result = HeaderUtils.getOptionalHeaderSingleValue(headers, "testHeader", String.class);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetOptionalHeaderSingleValue_invalidType_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("testHeader", "testValue");
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getOptionalHeaderSingleValue(headers, "testHeader", Integer.class));
    }

    @Test
    void testGetHeaderSingleValue_enumPassed_shouldReturnEnum() {
        Map<String, Object> headers = GeneralMockProvider.createHeadersWithSingleValue("Accept-Role", "CLIENT");
        Role role = HeaderUtils.getHeaderSingleValue(headers, "Accept-Role", Role.class);
        assertNotNull(role);
    }

    @Test
    void testGetHeaderSingleValue_withStompAccessor_validHeader_shouldSucceed() {
        StompHeaderAccessor accessor = GeneralMockProvider.createStompHeaderAccessorWithSingleValue("testHeader", "testValue");
        String result = HeaderUtils.getHeaderSingleValue(accessor, "testHeader", String.class);
        assertEquals("testValue", result);
    }

    @Test
    void testGetHeaderSingleValue_withStompAccessor_missingHeader_shouldThrowMissingWebSocketHeaderException() {
        StompHeaderAccessor accessor = GeneralMockProvider.createStompHeaderAccessorWithSingleValue("anotherHeader", "testValue");
        assertThrows(MissingWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(accessor, "testHeader", String.class));
    }

    @Test
    void testGetUser_withStompAccessor_userPresent_shouldReturnUserName() {
        StompHeaderAccessor accessor = GeneralMockProvider.createStompHeaderAccessorWithUser("testUser");
        String userName = HeaderUtils.getUser(accessor);
        assertEquals("testUser", userName);
    }

    @Test
    void testGetUser_withStompAccessor_noUser_shouldThrowMissingWebSocketHeaderException() {
        StompHeaderAccessor accessor = GeneralMockProvider.createStompHeaderAccessorWithoutUser();
        assertThrows(MissingWebSocketHeaderException.class, () -> HeaderUtils.getUser(accessor));
    }

    @Test
    void testGetLongFromDestination_validDestination_shouldReturnParsedLong() {
        StompHeaderAccessor accessor = GeneralMockProvider.createStompHeaderAccessorWithDestination("/topic/12345");
        Long result = HeaderUtils.getLongFromDestination(accessor, 2);
        assertEquals(12345L, result);
    }

    @Test
    void testGetLongFromDestination_invalidPosition_shouldThrowIndexOutOfBoundsException() {
        StompHeaderAccessor accessor = GeneralMockProvider.createStompHeaderAccessorWithDestination("/topic/12345");
        assertThrows(IndexOutOfBoundsException.class, () -> HeaderUtils.getLongFromDestination(accessor, 3));
    }
}

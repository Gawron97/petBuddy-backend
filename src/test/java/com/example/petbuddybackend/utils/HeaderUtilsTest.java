package com.example.petbuddybackend.utils;

import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import com.example.petbuddybackend.utils.header.HeaderUtils;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

public class HeaderUtilsTest {

    @Test
    void testGetHeaderSingleValue_validHeader_shouldSucceed() {
        Map<String, Object> headers = createHeadersWithSingleValue("testHeader", "testValue");
        String result = HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class);
        assertEquals("testValue", result);
    }

    @Test
    void testGetHeaderSingleValue_missingHeader_throwsMissingWebSocketHeaderException() {
        Map<String, Object> headers = createHeadersWithSingleValue("anotherHeader", "testValue");
        assertThrows(MissingWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class));
    }

    @Test
    void testGetHeaderSingleValue_invalidType_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = createHeadersWithSingleValue("testHeader", "testValue");
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", Integer.class));
    }

    @Test
    void testGetHeaderSingleValue_emptyHeader_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = createHeadersWithEmptyList("testHeader");
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getHeaderSingleValue(headers, "testHeader", String.class));
    }

    @Test
    void testGetHeaderSingleValue_multipleValuesInHeader_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = createHeadersWithMultipleValues("testHeader", List.of("value1", "value2"));
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
        Map<String, Object> headers = createHeadersWithSingleValue("testHeader", "testValue");
        Optional<String> result = HeaderUtils.getOptionalHeaderSingleValue(headers, "testHeader", String.class);
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get());
    }

    @Test
    void testGetOptionalHeaderSingleValue_missingHeader_shouldReturnEmptyOptional() {
        Map<String, Object> headers = createHeadersWithSingleValue("anotherHeader", "testValue");
        Optional<String> result = HeaderUtils.getOptionalHeaderSingleValue(headers, "testHeader", String.class);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetOptionalHeaderSingleValue_invalidType_shouldThrowInvalidWebSocketHeaderException() {
        Map<String, Object> headers = createHeadersWithSingleValue("testHeader", "testValue");
        assertThrows(InvalidWebSocketHeaderException.class, () ->
                HeaderUtils.getOptionalHeaderSingleValue(headers, "testHeader", Integer.class));
    }

    @Test
    void testGetHeaderSingleValue_enumPassed_shouldReturnEnum() {
        Map<String, Object> headers = createHeadersWithSingleValue("Accept-Role", "CLIENT");
        Role role = HeaderUtils.getHeaderSingleValue(headers, "Accept-Role", Role.class);
        assertNotNull(role);
    }

    private Map<String, Object> createHeadersWithSingleValue(String headerName, Object value) {
        Map<String, Object> nativeHeaders = new HashMap<>();
        nativeHeaders.put(headerName, Collections.singletonList(value));

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderUtils.NATIVE_HEADERS, nativeHeaders);

        return headers;
    }

    private Map<String, Object> createHeadersWithEmptyList(String headerName) {
        Map<String, Object> nativeHeaders = new HashMap<>();
        nativeHeaders.put(headerName, Collections.emptyList());

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderUtils.NATIVE_HEADERS, nativeHeaders);

        return headers;
    }

    private Map<String, Object> createHeadersWithMultipleValues(String headerName, List<Object> values) {
        Map<String, Object> nativeHeaders = new HashMap<>();
        nativeHeaders.put(headerName, values);

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderUtils.NATIVE_HEADERS, nativeHeaders);

        return headers;
    }
}

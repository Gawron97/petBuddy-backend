package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.*;

import static org.mockito.Mockito.mock;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class GeneralMockProvider {

    public static JwtAuthenticationToken createJwtToken(String email, String firstname, String lastname, String username) {
        return new JwtAuthenticationToken(mock(Jwt.class), null, null) {
            @Override
            public Map<String, Object> getTokenAttributes() {
                return Map.of(
                        "email", email,
                        "given_name", firstname,
                        "family_name", lastname,
                        "preferred_username", username
                );
            }
        };
    }

    public static Map<String, Object> createHeadersWithSingleValue(String headerName, Object value) {
        Map<String, Object> nativeHeaders = new HashMap<>();
        nativeHeaders.put(headerName, Collections.singletonList(value));

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderUtils.NATIVE_HEADERS, nativeHeaders);

        return headers;
    }

    public static Map<String, Object> createHeadersWithEmptyList(String headerName) {
        Map<String, Object> nativeHeaders = new HashMap<>();
        nativeHeaders.put(headerName, Collections.emptyList());

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderUtils.NATIVE_HEADERS, nativeHeaders);

        return headers;
    }

    public static Map<String, Object> createHeadersWithMultipleValues(String headerName, List<Object> values) {
        Map<String, Object> nativeHeaders = new HashMap<>();
        nativeHeaders.put(headerName, values);

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderUtils.NATIVE_HEADERS, nativeHeaders);

        return headers;
    }

    public static StompHeaderAccessor createStompHeaderAccessorWithSingleValue(String testHeader, String testValue) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader(testHeader, testValue);
        accessor.setLeaveMutable(false);

        return accessor;
    }

    public static StompHeaderAccessor createStompHeaderAccessorWithUser(String testUser) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Principal principal = () -> testUser;

        accessor.setUser(principal);
        accessor.setLeaveMutable(false);

        return accessor;
    }

    public static StompHeaderAccessor createStompHeaderAccessorWithoutUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setUser(null);
        accessor.setLeaveMutable(false);

        return accessor;
    }

    public static StompHeaderAccessor createStompHeaderAccessorWithDestination(String path) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);

        accessor.setDestination(path);
        accessor.setLeaveMutable(false);

        return accessor;
    }
}

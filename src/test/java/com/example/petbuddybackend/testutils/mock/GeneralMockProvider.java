package com.example.petbuddybackend.testutils.mock;

import com.example.petbuddybackend.utils.header.HeaderUtils;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
}

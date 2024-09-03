package com.example.petbuddybackend.utils.header;

import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderUtils {

    public static final String NATIVE_HEADERS = "nativeHeaders";
    private static final String INVALID_TYPE_MESSAGE = "Header \"%s\" is not of type \"%s\"";
    private static final String NO_VALUE_MESSAGE = "Header \"%s\" has no value";
    private static final String MULTIPLE_VALUES_MESSAGE = "Header \"%s\" has multiple values";
    private static final String HEADERS_ARE_NOT_OF_TYPE_MAP_MESSAGE = "Headers are not of type Map";

    /**
     * Extract first value of header from headers map
     * */
    public static <T> T getHeaderSingleValue(Map<String, Object> headers, String headerName, Class<T> type) {
        Map<String, Object> nativeHeaders = extractNativeHeaders(headers);
        checkHeaderExists(nativeHeaders, headerName);
        return extractSingleHeaderValue(nativeHeaders, headerName, type);
    }

    /**
     * Extract first value of header from headers map
     * */
    public static <T> Optional<T> getOptionalHeaderSingleValue(Map<String, Object> headers, String headerName, Class<T> type) {
        Map<String, Object> nativeHeaders = extractNativeHeaders(headers);
        if(!nativeHeaders.containsKey(headerName)) {
            return Optional.empty();
        }
        return Optional.of(extractSingleHeaderValue(nativeHeaders, headerName, type));
    }

    public static <T> T getHeaderSingleValue(StompHeaderAccessor accessor, String headerName, Class<T> type) {
        return getOptionalHeaderSingleValue(accessor, headerName, type).orElseThrow(
                () -> new MissingWebSocketHeaderException(headerName)
        );
    }

    public static <T> Optional<T> getOptionalHeaderSingleValue(StompHeaderAccessor accessor, String headerName, Class<T> type) {
        String value = accessor.getFirstNativeHeader(headerName);

        if(value == null) {
            return Optional.empty();
        }

        return Optional.of(castToType(headerName, value, type));
    }

    public static String getUser(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();

        if(user == null) {
            throw new MissingWebSocketHeaderException("User");
        }

        return user.getName();
    }

    public static Long getLongFromDestination(StompHeaderAccessor accessor, int position) {
        String destination = accessor.getDestination();

        String[] parts = destination.split("/");
        if (parts.length > position) {
            return Long.parseLong(parts[position]);
        }

        throw new IndexOutOfBoundsException("Position pointing to out of bounds element");
    }

    @SuppressWarnings("unchecked")
    private static <T> T extractSingleHeaderValue(Map<String, Object> nativeHeaders, String headerName, Class<T> type) {
        Object header = nativeHeaders.get(headerName);

        if(!(header instanceof List<?>)) {
            throw new InvalidWebSocketHeaderException(String.format(INVALID_TYPE_MESSAGE, headerName, List.class.getSimpleName()));
        }

        List<Object> headerList = (List<Object>) header;

        if(headerList.isEmpty()) {
            throw new InvalidWebSocketHeaderException(String.format(NO_VALUE_MESSAGE, headerName));
        }

        if(headerList.size() > 1) {
            throw new InvalidWebSocketHeaderException(MULTIPLE_VALUES_MESSAGE);
        }

        return castToType(headerName, headerList.get(0).toString(), type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T castToType(String headerName, String value, Class<T> type) {
        if(type.isEnum()) {
            return type.cast(Enum.valueOf((Class<Enum>) type, value));
        }

        if (type.isInstance(value)) {
            return type.cast(value);
        }

        throw new InvalidWebSocketHeaderException(String.format(INVALID_TYPE_MESSAGE, headerName, type.getSimpleName()));
    }

    private static void checkHeaderExists(Map<String, Object> nativeHeaders, String headerName) {
        if(!nativeHeaders.containsKey(headerName)) {
            throw new MissingWebSocketHeaderException(headerName);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractNativeHeaders(Map<String, Object> headers) {
        Object nativeHeaders = headers.get(NATIVE_HEADERS);

        if(nativeHeaders instanceof Map<?,?>) {
            return (Map<String, Object>) nativeHeaders;
        }

        throw new IllegalArgumentException(HEADERS_ARE_NOT_OF_TYPE_MAP_MESSAGE);
    }
}

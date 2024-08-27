package com.example.petbuddybackend.utils.header;

import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

// TODO: test
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderUtils {

    public static boolean headerExists(Map<String, Object> headers, String headerName) {
        return headers.containsKey(headerName);
    }

    public static <T> T getHeader(Map<String, Object> headers, String headerName, Class<T> type) {
        checkHeaderExists(headers, headerName);
        Object header = headers.get(headerName);

        if (type.isInstance(header)) {
            return type.cast(header);
        }

        throw new InvalidWebSocketHeaderException("Header " + headerName + " is not of type " + type.getSimpleName());
    }

    public static <T> Optional<T> getOptionalHeader(Map<String, Object> headers, String headerName, Class<T> type) {
        Object header = headers.get(headerName);

        if(header == null) {
            return Optional.empty();
        }

        if (type.isInstance(header)) {
            return Optional.of(type.cast(header));
        }

        throw new InvalidWebSocketHeaderException("Header " + headerName + " is not of type " + type.getSimpleName());
    }

    private static void checkHeaderExists(Map<String, Object> headers, String headerName) {
        if(!headerExists(headers, headerName)) {
            throw new MissingWebSocketHeaderException(headerName);
        }
    }
}

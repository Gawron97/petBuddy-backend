package com.example.petbuddybackend.utils.header;

import com.example.petbuddybackend.utils.exception.throweable.websocket.InvalidWebSocketHeaderException;
import com.example.petbuddybackend.utils.exception.throweable.websocket.MissingWebSocketHeaderException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderUtils {

    public static final String NATIVE_HEADERS = "nativeHeaders";

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

    private static <T> T extractSingleHeaderValue(Map<String, Object> nativeHeaders, String headerName, Class<T> type) {
        Object header = nativeHeaders.get(headerName);

        if(header instanceof List<?> headerList) {
            if(headerList.isEmpty()) {
                throw new InvalidWebSocketHeaderException("Header " + headerName + " has no value");
            }

            if(headerList.size() > 1) {
                throw new InvalidWebSocketHeaderException("Header " + headerName + " has more than one value");
            }

            Object headerListObj = headerList.get(0);

            if(type.isEnum()) {
                return type.cast(Enum.valueOf((Class<Enum>) type, headerListObj.toString()));
            }

            if (type.isInstance(headerListObj)) {
                return type.cast(headerListObj);
            }
        }

        throw new InvalidWebSocketHeaderException("Header " + headerName + " is not of type " + type.getSimpleName());
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

        throw new IllegalArgumentException("Headers are not of type Map");
    }
}

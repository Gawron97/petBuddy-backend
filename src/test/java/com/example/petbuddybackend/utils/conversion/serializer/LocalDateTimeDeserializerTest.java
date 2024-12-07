package com.example.petbuddybackend.utils.conversion.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocalDateTimeDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserialize_validLocalDateTime() throws IOException {
        String localDateTimeString = "\"2024-09-03 10:00:00\"";
        LocalDateTime expected = LocalDateTime.of(2024, 9, 3, 10, 0, 0, 0);

        LocalDateTime actual = objectMapper.readValue(localDateTimeString, LocalDateTime.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testDeserialize_invalidFormat_throwsException() {
        String invalidLocalDateTimeString = "\"2024-09-03T10:15:30\"";

        assertThrows(
                DateTimeParseException.class,
                () -> objectMapper.readValue(invalidLocalDateTimeString, LocalDateTime.class)
        );
    }
}

package com.example.petbuddybackend.utils.conversion.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ZonedDateTimeDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void testDeserialize_validZonedDateTime() throws IOException {
        String zonedDateTimeString = "\"2024-09-03 10:00:00.000 +0100\"";
        ZonedDateTime expected = ZonedDateTime.of(
                2024, 9, 3, 10, 0, 0, 0, ZoneId.of("+0100")
        );

        ZonedDateTime actual = objectMapper.readValue(zonedDateTimeString, ZonedDateTime.class);

        assertEquals(expected, actual);
    }

    @Test
    public void testDeserialize_invalidFormat_throwsException() {
        String invalidZonedDateTimeString = "\"2024-09-03 10:15:30\"";

        assertThrows(
                DateTimeParseException.class,
                () -> objectMapper.readValue(invalidZonedDateTimeString, ZonedDateTime.class)
        );
    }
}

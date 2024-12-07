package com.example.petbuddybackend.utils.conversion.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalDateTimeSerializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void testSerialize_validLocalDateTime() throws IOException {
        LocalDateTime localDateTime = LocalDateTime.of(2024, 9, 3, 10, 0, 0, 0);
        String expected = "\"2024-09-03 10:00:00\"";

        String actual = objectMapper.writeValueAsString(localDateTime);

        assertEquals(expected, actual);
    }
}

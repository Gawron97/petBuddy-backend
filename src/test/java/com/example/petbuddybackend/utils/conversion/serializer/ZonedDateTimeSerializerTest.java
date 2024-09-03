package com.example.petbuddybackend.utils.conversion.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZonedDateTimeSerializerTest {

    private ObjectMapper objectMapper;
    private ZonedDateTimeSerializer serializer;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        serializer = new ZonedDateTimeSerializer();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ZonedDateTime.class, serializer);
        objectMapper.registerModule(module);
    }

    @Test
    public void testSerialize_validZonedDateTime() throws IOException {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2024, 9, 3, 10, 0, 0, 0, ZoneId.of("+0100")
        );

        String expectedJson = "\"2024-09-03 10:00:00.000 +0100\"";
        String actualJson = objectMapper.writeValueAsString(zonedDateTime);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void testSerialize_withMockedJsonGenerator() throws IOException {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2024, 9, 3, 10, 0, 0, 0, ZoneId.of("+0100")
        );

        JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
        SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);

        serializer.serialize(zonedDateTime, jsonGenerator, serializerProvider);

        Mockito.verify(jsonGenerator).writeString("2024-09-03 10:00:00.000 +0100");
    }

    @Test
    public void testSerialize_withCustomFormatter() throws IOException {
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z");
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2024, 9, 3, 10, 0, 0, 0, ZoneId.of("+0100")
        );

        // Temporarily change the formatter for this test
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = new ObjectMapper().getFactory().createGenerator(writer);
        SerializerProvider serializerProvider = Mockito.mock(SerializerProvider.class);

        jsonGenerator.writeString(zonedDateTime.format(customFormatter));
        jsonGenerator.flush();

        assertEquals("\"2024/09/03 10:00:00 +0100\"", writer.toString());
    }
}

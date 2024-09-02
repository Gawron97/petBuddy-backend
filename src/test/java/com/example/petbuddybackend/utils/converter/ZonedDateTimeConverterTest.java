package com.example.petbuddybackend.utils.converter;

import com.example.petbuddybackend.utils.time.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class ZonedDateTimeConverterTest {

    private ZonedDateTimeConverter converter = new ZonedDateTimeConverter();
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TimeUtils.ZONED_TIMESTAMP_FORMAT);

    @BeforeEach
    void setUp() {
        converter = new ZonedDateTimeConverter();
    }

    @Test
    void testConvert_WhenSourceIsValid_ShouldReturnZonedDateTime() {
        // Given
        String source = "2020-01-07 12:05:05.123 +0100";

        // When
        ZonedDateTime result = converter.convert(source);

        // Then
        assertNotNull(result);
        assertEquals(ZonedDateTime.parse(source, FORMATTER), result);
    }

    @Test
    void testConvert_WhenSourceIsInValid_ShouldThrowDateTimeParseException() {
        // Given
        String source = "2020-01-07 12:05:05.123";

        // When
        assertThrows(DateTimeParseException.class, () -> converter.convert(source));
    }

}

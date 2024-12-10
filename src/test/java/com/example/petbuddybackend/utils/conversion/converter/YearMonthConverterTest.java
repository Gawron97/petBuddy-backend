package com.example.petbuddybackend.utils.conversion.converter;

import com.example.petbuddybackend.utils.time.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.*;

public class YearMonthConverterTest {

    private YearMonthConverter yearMonthConverter = new YearMonthConverter();
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TimeUtils.YEAR_MONTH_FORMAT);

    @BeforeEach
    void setUp() {
        yearMonthConverter = new YearMonthConverter();
    }

    @Test
    void testConvert_WhenSourceIsValid_ShouldReturnProperYearMonth() {
        //Given
        String source = "2021-01";

        //When
        var result = yearMonthConverter.convert(source);

        //Then
        assertNotNull(result);
        assertEquals(YearMonth.parse(source, FORMATTER), result);
    }

    @Test
    void testConvert_WhenSourceNotValid_ShouldThrowDateTimeParseException() {
        //Given
        String source = "2021-13-01";

        //When Then
        assertThrows(DateTimeParseException.class, () -> yearMonthConverter.convert(source));
    }

}

package com.example.petbuddybackend.utils.time;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DateTimeException;
import java.time.zone.ZoneRulesException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeUtilsTest {

    @ParameterizedTest
    @MethodSource("provideStringsForConversionToTimeZone")
    void testStringConversionToTimeZone_shouldSucceed(String timeZone) {
        assertNotNull(TimeUtils.getOrSystemDefault(timeZone));
    }

    @ParameterizedTest
    @MethodSource("provideBadStringsForConversionToTimeZone")
    void testStringConversionToTimeZone_shouldFail(String timeZone, Class<? extends Throwable> exceptionType) {
        assertThrows(
                exceptionType,
                () -> TimeUtils.getOrSystemDefault(timeZone)
        );
    }

    private static Stream<String> provideStringsForConversionToTimeZone() {
        return Stream.of(
                "UTC",
                "CET",
                "Asia/Tokyo",
                "America/New_York",
                "Europe/Warsaw",
                "+02:00",
                null
        );
    }

    private static Stream<Arguments> provideBadStringsForConversionToTimeZone() {
        return Stream.of(
                Arguments.of(
                        "+00:0",
                        DateTimeException.class
                ),
                Arguments.of(
                        "CCCT",
                        ZoneRulesException.class
                )
        );
    }
}

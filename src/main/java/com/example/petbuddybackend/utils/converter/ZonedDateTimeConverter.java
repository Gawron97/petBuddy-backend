package com.example.petbuddybackend.utils.converter;

import com.example.petbuddybackend.utils.time.TimeUtils;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TimeUtils.ZONED_TIMESTAMP_FORMAT);

    @Override
    public ZonedDateTime convert(String source) {
        return ZonedDateTime.parse(source, FORMATTER);
    }

}

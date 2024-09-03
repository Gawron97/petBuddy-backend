package com.example.petbuddybackend.utils.conversion.converter;

import com.example.petbuddybackend.utils.conversion.DefaultFormatter;
import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;

public class ZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(String source) {
        return ZonedDateTime.parse(source, DefaultFormatter.ZONED_DATETIME_FORMATTER);
    }

}

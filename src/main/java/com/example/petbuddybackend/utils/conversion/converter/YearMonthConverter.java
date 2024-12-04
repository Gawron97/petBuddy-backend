package com.example.petbuddybackend.utils.conversion.converter;

import com.example.petbuddybackend.utils.conversion.DefaultFormatter;
import org.springframework.core.convert.converter.Converter;

import java.time.YearMonth;

public class YearMonthConverter implements Converter<String, YearMonth> {

    @Override
    public YearMonth convert(String source) {
        return YearMonth.parse(source, DefaultFormatter.YEAR_MONTH_FORMATTER);
    }

}

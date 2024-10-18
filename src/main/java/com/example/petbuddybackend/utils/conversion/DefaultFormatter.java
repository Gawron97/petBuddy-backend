package com.example.petbuddybackend.utils.conversion;

import com.example.petbuddybackend.utils.time.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultFormatter {

    public static final DateTimeFormatter ZONED_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(TimeUtils.ZONED_DATETIME_FORMAT);

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(TimeUtils.DATE_FORMAT);
}

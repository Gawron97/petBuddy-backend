package com.example.petbuddybackend.utils.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ZONED_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS Z";
    public static final String YEAR_MONTH_FORMAT = "yyyy-MM";

    public static ZoneId get(String acceptTimezone) {
        return ZoneId.of(acceptTimezone);
    }

    public static ZoneId getOrSystemDefault(String acceptTimezone) {
        if(acceptTimezone == null) {
            return ZoneId.systemDefault();
        }
        return get(acceptTimezone);
    }
}

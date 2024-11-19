package com.example.petbuddybackend.dto.care;

import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record CareStatusesHistoryDTO(
        @JsonFormat(pattern = TimeUtils.ZONED_DATETIME_FORMAT)
        ZonedDateTime createdAt,
        CareStatus clientStatus,
        CareStatus caretakerStatus
) {
}

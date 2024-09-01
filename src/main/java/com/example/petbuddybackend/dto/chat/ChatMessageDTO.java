package com.example.petbuddybackend.dto.chat;

import com.example.petbuddybackend.utils.time.TimeUtils;
import com.example.petbuddybackend.utils.serializers.ZonedDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
        private Long id;
        private Long chatId;
        private String senderEmail;
        private String content;
        @JsonFormat(pattern = TimeUtils.ZONED_DATETIME_FORMAT)
        @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
        private ZonedDateTime createdAt;
}

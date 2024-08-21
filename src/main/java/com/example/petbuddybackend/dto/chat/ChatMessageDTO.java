package com.example.petbuddybackend.dto.chat;

import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(pattern = TimeUtils.ZONED_TIMESTAMP_FORMAT)
        private ZonedDateTime createdAt;
}

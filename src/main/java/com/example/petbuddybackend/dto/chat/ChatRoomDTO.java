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
public class ChatRoomDTO {
    private Long id;
    private String chatterEmail;
    private String chatterName;
    private String chatterSurname;
    @JsonFormat(pattern = TimeUtils.ZONED_DATETIME_FORMAT)
    private ZonedDateTime lastMessageCreatedAt;
    private String lastMessage;
    private String lastMessageSendBy;
    private Boolean seen;
}

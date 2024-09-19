package com.example.petbuddybackend.service.chat.session;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZoneId;

@Data
@AllArgsConstructor
class ChatUserMetadata {
    private String username;
    private String sessionId;
    private ZoneId zoneId;
}

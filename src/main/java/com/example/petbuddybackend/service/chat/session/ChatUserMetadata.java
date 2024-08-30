package com.example.petbuddybackend.service.chat.session;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZoneId;

@Data
@AllArgsConstructor
class ChatUserMetadata {
    private String username;
    private ZoneId zoneId;

    public ZoneId updateZoneId(ZoneId zoneId) {
        if(zoneId == null) {
            return this.zoneId;
        }

        this.zoneId = zoneId;
        return this.zoneId;
    }
}

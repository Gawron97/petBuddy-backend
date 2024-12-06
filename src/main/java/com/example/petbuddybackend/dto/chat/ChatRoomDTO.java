package com.example.petbuddybackend.dto.chat;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private AccountDataDTO chatter;
    private ChatMessageDTO lastMessage;
    private boolean blocked;
}

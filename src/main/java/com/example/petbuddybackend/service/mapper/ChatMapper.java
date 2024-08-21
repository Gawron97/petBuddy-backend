package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ChatMapper {

    ChatMapper INSTANCE = Mappers.getMapper(ChatMapper.class);

    @Mapping(target = "senderEmail", source = "sender.email")
    @Mapping(target = "chatId", source = "chatRoom.id")
    ChatMessageDTO mapToChatMessageDTO(ChatMessage chatMessage);
}

package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.chat.ChatMessageDTO;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper
public interface ChatMapper {

    ChatMapper INSTANCE = Mappers.getMapper(ChatMapper.class);

    @Mapping(target = "senderEmail", source = "sender.email")
    @Mapping(target = "chatId", source = "chatRoom.id")
    ChatMessageDTO mapToChatMessageDTO(ChatMessage chatMessage);

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "mapToZonedDateTime")
    ChatMessageDTO mapTimeZone(ChatMessageDTO chatMessage, @Context ZoneId zoneId);

    @Mapping(target = "lastMessageCreatedAt", source = "lastMessageCreatedAt", qualifiedByName = "mapToZonedDateTime")
    ChatRoomDTO mapTimeZone(ChatRoomDTO chatRoom, @Context ZoneId zoneId);

    @Named("mapToZonedDateTime")
    default ZonedDateTime mapToZonedDateTime(ZonedDateTime date, @Context ZoneId zoneId) {
        return date.withZoneSameInstant(zoneId);
    }
}
